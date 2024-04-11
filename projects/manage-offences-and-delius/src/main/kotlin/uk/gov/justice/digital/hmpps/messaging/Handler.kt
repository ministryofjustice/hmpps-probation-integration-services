package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.entity.DetailedOffence
import uk.gov.justice.digital.hmpps.entity.OffenceRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceOffence
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.repository.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.repository.findCourtCategory
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val manageOffencesClient: ManageOffencesClient,
    private val detailedOffenceRepository: DetailedOffenceRepository,
    private val offenceRepository: OffenceRepository,
    private val referenceDataRepository: ReferenceDataRepository
) : NotificationHandler<HmppsDomainEvent> {

    @Transactional
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val offence = manageOffencesClient.getOffence(notification.message.offenceCode)
        offence.createOrMerge()
        val existingEntity = detailedOffenceRepository.findByCode(offence.code)
        detailedOffenceRepository.save(existingEntity.mergeWith(offence.newEntity))
        telemetryService.trackEvent(
            if (existingEntity == null) "OffenceCodeCreated" else "OffenceCodeUpdated",
            mapOf("offenceCode" to offence.code)
        )
    }

    val Offence.newEntity
        get() = DetailedOffence(
            code = code,
            description = description,
            startDate = startDate,
            endDate = endDate,
            homeOfficeCode = homeOfficeStatsCode,
            homeOfficeDescription = homeOfficeDescription,
            legislation = legislation,
            category = referenceDataRepository.findCourtCategory(offenceType)
                ?: throw NotFoundException("Court category", "code", offenceType)
        )

    fun DetailedOffence?.mergeWith(newEntity: DetailedOffence) = this?.copy(
        code = newEntity.code,
        description = newEntity.description,
        startDate = newEntity.startDate,
        endDate = newEntity.endDate,
        homeOfficeCode = newEntity.homeOfficeCode,
        homeOfficeDescription = newEntity.homeOfficeDescription,
        legislation = newEntity.legislation,
        category = newEntity.category
    ) ?: newEntity

    private fun Offence.createOrMerge() {
        if (homeOfficeStatsCode != null && homeOfficeDescription != null) {
            val highLevelOffence = checkNotNull(offenceRepository.findOffenceByCode(highLevelCode!!)) {
                "High Level Offence not found: $highLevelCode"
            }

            val offence = offenceRepository.findOffenceByCode(homeOfficeCode).mergeWith(asReference(highLevelOffence))
            offenceRepository.save(offence)
        }
    }
}

val HmppsDomainEvent.offenceCode get() = additionalInformation["offenceCode"] as String

val Offence.mainCategoryCode get() = homeOfficeStatsCode?.take(3)
val Offence.subCategoryCode get() = homeOfficeStatsCode?.takeLast(2)
val Offence.highLevelCode get() = mainCategoryCode?.let { it + "00" }
val Offence.homeOfficeCode get() = mainCategoryCode + subCategoryCode
fun Offence.asReference(highLevelOffence: ReferenceOffence) = ReferenceOffence(
    code = homeOfficeCode,
    description = homeOfficeDescription!!,
    mainCategoryCode = mainCategoryCode!!,
    selectable = false,
    mainCategoryDescription = highLevelOffence.description,
    mainCategoryAbbreviation = highLevelOffence.description.take(50),
    ogrsOffenceCategoryId = highLevelOffence.ogrsOffenceCategoryId,
    subCategoryCode = subCategoryCode!!,
    subCategoryDescription = "$homeOfficeDescription - $subCategoryCode",
    form20Code = highLevelOffence.form20Code,
    childAbduction = null,
    schedule15SexualOffence = null,
    schedule15ViolentOffence = null
)

fun ReferenceOffence?.mergeWith(referenceOffence: ReferenceOffence) = this?.copy(
    code = code,
    description = description,
    mainCategoryCode = mainCategoryCode,
    mainCategoryDescription = mainCategoryDescription,
    mainCategoryAbbreviation = mainCategoryAbbreviation,
    ogrsOffenceCategoryId = ogrsOffenceCategoryId,
    subCategoryCode = subCategoryCode,
    subCategoryDescription = subCategoryDescription,
    form20Code = form20Code,
    schedule15SexualOffence = schedule15SexualOffence,
    schedule15ViolentOffence = schedule15ViolentOffence,
    childAbduction = childAbduction
) ?: referenceOffence
