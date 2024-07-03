package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.config.IgnoredOffence.Companion.IGNORED_OFFENCES
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.entity.DetailedOffence
import uk.gov.justice.digital.hmpps.entity.OffenceRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceOffence
import uk.gov.justice.digital.hmpps.entity.getByCode
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.repository.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.repository.findCourtCategory
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
@Transactional
@Channel("manage-offences-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val manageOffencesClient: ManageOffencesClient,
    private val detailedOffenceRepository: DetailedOffenceRepository,
    private val offenceRepository: OffenceRepository,
    private val referenceDataRepository: ReferenceDataRepository
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(name = "manage-offences/offence-changed")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)

        val offence = manageOffencesClient.getOffence(notification.message.offenceCode)

        IGNORED_OFFENCES.firstOrNull { it.matches(offence) }?.let {
            telemetryService.trackEvent("OffenceCodeIgnored", offence.telemetry + mapOf("reason" to it.reason))
            return
        }

        mergeDetailedOffence(offence)
        val isNew = createReferenceOffence(offence)

        if (isNew) {
            telemetryService.trackEvent("OffenceCodeCreated", offence.telemetry)
        }
    }

    private fun mergeDetailedOffence(offence: Offence) {
        val existingEntity = detailedOffenceRepository.findByCode(offence.code)
        detailedOffenceRepository.save(existingEntity.mergeWith(offence.toDetailedOffence()))
    }

    private fun createReferenceOffence(offence: Offence): Boolean {
        val homeOfficeCode = offence.homeOfficeCode
        return if (homeOfficeCode != null && offenceRepository.findByCode(homeOfficeCode) == null) {
            val highLevelOffence = offenceRepository.getByCode(offence.highLevelCode!!)
            offenceRepository.save(offence.toReferenceOffence(highLevelOffence))
            true
        } else false // entity already exists, don't update it
    }

    private fun Offence.toDetailedOffence() = DetailedOffence(
        code = code,
        description = description,
        startDate = startDate,
        endDate = endDate,
        homeOfficeCode = homeOfficeStatsCode,
        homeOfficeDescription = homeOfficeDescription,
        legislation = legislation,
        category = referenceDataRepository.findCourtCategory(offenceType)
            ?: throw NotFoundException("Court category", "code", offenceType),
        schedule15ViolentOffence = schedule15ViolentOffence,
        schedule15SexualOffence = schedule15SexualOffence
    )

    private fun Offence.toReferenceOffence(highLevelOffence: ReferenceOffence) = ReferenceOffence(
        code = homeOfficeCode!!,
        description = "$homeOfficeDescription - $homeOfficeCode",
        mainCategoryCode = mainCategoryCode!!,
        selectable = false,
        mainCategoryDescription = highLevelOffence.description.take(200),
        mainCategoryAbbreviation = highLevelOffence.description.take(50),
        ogrsOffenceCategoryId = highLevelOffence.ogrsOffenceCategoryId,
        subCategoryCode = subCategoryCode!!,
        subCategoryDescription = homeOfficeDescription!!.take(200),
        form20Code = highLevelOffence.form20Code,
        childAbduction = null,
        schedule15ViolentOffence = schedule15ViolentOffence,
        schedule15SexualOffence = schedule15SexualOffence
    )

    private fun DetailedOffence?.mergeWith(newEntity: DetailedOffence) = this?.apply {
        code = newEntity.code
        description = newEntity.description
        startDate = newEntity.startDate
        endDate = newEntity.endDate
        homeOfficeCode = newEntity.homeOfficeCode
        homeOfficeDescription = newEntity.homeOfficeDescription
        legislation = newEntity.legislation
        category = newEntity.category
    } ?: newEntity
}

val HmppsDomainEvent.offenceCode get() = additionalInformation["offenceCode"] as String

val Offence.telemetry
    get() = listOfNotNull(
        "offenceCode" to code,
        homeOfficeCode?.let { "homeOfficeCode" to it }
    ).toMap()