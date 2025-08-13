package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.dto.InsertRemandDTO
import uk.gov.justice.digital.hmpps.dto.InsertRemandResult

@Service
class RemandService(
    private val personService: PersonService,
    private val eventService: EventService,
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
            hearingId = insertRemandDTO.hearingId,
            additionalOffences = insertRemandDTO.additionalOffences,
        )

        return InsertRemandResult(insertPersonResult, insertEventResult)
    }
}