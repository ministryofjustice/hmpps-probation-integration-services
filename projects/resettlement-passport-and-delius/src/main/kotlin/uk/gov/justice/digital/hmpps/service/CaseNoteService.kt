package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CreateContact
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.entity.*

@Service
class CaseNoteService(
    auditedInteractionService: AuditedInteractionService,
    private val caseNoteRepository: CaseNoteRepository,
    private val personRepository: PersonRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val authorService: AuthorService,
) : AuditableService(auditedInteractionService) {

    fun createContact(crn: String, createContact: CreateContact) {
        val person = personRepository.getByCrn(crn)
        val type = caseNoteTypeRepository.getCode(createContact.type.code)
        val authorDetails = authorService.authorDetails(createContact.author)

        val caseNote = CaseNote(
            person = person,
            date = createContact.dateTime.toLocalDate(),
            startTime = createContact.dateTime,
            notes = createContact.notes,
            staff = authorDetails.staff,
            probationAreaId = authorDetails.probationArea.id,
            team = authorDetails.team,
            type = type
        )
        caseNoteRepository.save(caseNote)
    }


}
