package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CreateContact
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.InvalidEstablishmentCodeException
import uk.gov.justice.digital.hmpps.model.StaffName

@Service
class CaseNoteService(
    auditedInteractionService: AuditedInteractionService,
    private val caseNoteRepository: CaseNoteRepository,
    private val personRepository: PersonRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val assignmentService: AssignmentService,
) : AuditableService(auditedInteractionService) {

    fun createContact(crn: String, createContact: CreateContact) = audit(BusinessInteractionCode.ADD_CONTACT) {
        val person = personRepository.getByCrn(crn)
        val type = caseNoteTypeRepository.getCode(createContact.type.code)

        try {
            val prisonStaff = assignmentService.findAssignment(
                createContact.author.prisonCode,
                StaffName(createContact.author.forename, createContact.author.surname)
            )
            val caseNote = CaseNote(
                person = person,
                date = createContact.dateTime.toLocalDate(),
                startTime = createContact.dateTime,
                notes = createContact.notes,
                staffId = prisonStaff.third,
                teamId = prisonStaff.second,
                probationAreaId = prisonStaff.first,
                type = type
            )
            caseNoteRepository.save(caseNote)
        } catch (ex: Exception) {
            when (ex) {
                is InvalidEstablishmentCodeException,
                is NotFoundException -> throw InvalidRequestException(ex.message!!)

                else -> throw ex
            }
        }
    }
}
