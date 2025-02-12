package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.dto.InsertRemandDTO
import uk.gov.justice.digital.hmpps.dto.InsertRemandResult
import uk.gov.justice.digital.hmpps.messaging.Notifier
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class RemandService(
    private val personService: PersonService,
    private val eventService: EventService,
    private val telemetryService: TelemetryService,
    private val notifier: Notifier
) {
    @Transactional
    fun insertPersonOnRemand(insertRemandDTO: InsertRemandDTO): InsertRemandResult {
        val insertPersonResult = personService.insertPerson(
            defendant = insertRemandDTO.defendant,
            courtCode = insertRemandDTO.courtCode
        )

        val insertEventResult = eventService.insertEvent(
            hearingOffence = insertRemandDTO.hearingOffence,
            person = insertPersonResult.person,
            courtCode = insertRemandDTO.courtCode,
            sittingDay = insertRemandDTO.sittingDay,
            caseUrn = insertRemandDTO.caseUrn,
            hearingId = insertRemandDTO.hearingId
        )

        notifier.caseCreated(insertPersonResult.person)
        insertPersonResult.address?.let { notifier.addressCreated(it) }

        telemetryService.trackEvent(
            "PersonCreated", mapOf(
                "hearingId" to insertRemandDTO.hearingId,
                "defendantId" to insertRemandDTO.defendant.id,
                "CRN" to insertPersonResult.person.crn,
                "personId" to insertPersonResult.person.id.toString(),
                "personManagerId" to insertPersonResult.personManager.id.toString(),
                "equalityId" to insertPersonResult.equality.id.toString(),
                "addressId" to insertPersonResult.address?.id.toString(),
            )
        )

        telemetryService.trackEvent(
            "EventCreated", mapOf(
                "hearingId" to insertRemandDTO.hearingId,
                "eventId" to insertEventResult.event.id.toString(),
                "eventNumber" to insertEventResult.event.number,
                "CRN" to insertEventResult.event.person.crn,
                "personId" to insertEventResult.event.person.id.toString(),
                "orderManagerId" to insertEventResult.orderManager.id.toString(),
                "mainOffenceId" to insertEventResult.mainOffence.id.toString(),
                "courtAppearanceId" to insertEventResult.courtAppearance.id.toString(),
                "contactId" to insertEventResult.contact.id.toString()
            )
        )

        return InsertRemandResult(insertPersonResult, insertEventResult)
    }
}