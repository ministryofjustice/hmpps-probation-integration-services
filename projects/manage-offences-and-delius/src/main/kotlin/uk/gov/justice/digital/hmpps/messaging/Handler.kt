package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.entity.DetailedOffence
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
    private val referenceDataRepository: ReferenceDataRepository
) : NotificationHandler<HmppsDomainEvent> {

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val offence = manageOffencesClient.getOffence(notification.message.offenceCode)
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
}

val HmppsDomainEvent.offenceCode get() = additionalInformation["offenceCode"] as String
