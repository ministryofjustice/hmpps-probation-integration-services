package uk.gov.justice.digital.hmpps.integrations.delius.service

import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.config.defaultTypeForNonNsi
import uk.gov.justice.digital.hmpps.config.setDescription
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.CASE_NOTES_MERGE
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.model.isAlertType
import uk.gov.justice.digital.hmpps.integrations.delius.repository.*
import java.time.temporal.ChronoUnit

@Service
class DeliusService(
    auditedInteractionService: AuditedInteractionService,
    private val caseNoteRepository: CaseNoteRepository,
    private val nomisTypeRepository: CaseNoteNomisTypeRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val offenderRepository: OffenderRepository,
    private val assignmentService: AssignmentService,
    private val relatedService: CaseNoteRelatedService,
    private val featureFlags: FeatureFlags
) : AuditableService(auditedInteractionService) {
    @Transactional
    fun mergeCaseNote(@Valid caseNote: DeliusCaseNote) = audit(CASE_NOTES_MERGE) {
        it["nomisId"] = caseNote.header.noteId

        val existing = caseNoteRepository.findByNomisId(caseNote.header.noteId)

        val entity = if (existing == null) caseNote.newEntity() else existing.updateFrom(caseNote)
        if (entity != null) {
            caseNoteRepository.save(entity)
            it["contactId"] = entity.id
        }
    }

    private fun CaseNote.updateFrom(caseNote: DeliusCaseNote): CaseNote? {
        val last = lastModifiedDateTime.truncatedTo(ChronoUnit.SECONDS)
        val current = caseNote.body.systemTimestamp.truncatedTo(ChronoUnit.SECONDS)
        return if (last.isBefore(current)) {
            copy(
                notes = caseNote.body.notes(notes.length),
                date = caseNote.body.contactTimeStamp,
                startTime = caseNote.body.contactTimeStamp,
                lastModifiedDateTime = caseNote.body.systemTimestamp
            )
        } else {
            log.warn("Case Note update ignored because it was out of sequence ${caseNote.header}")
            null
        }
    }

    private fun DeliusCaseNote.newEntity(): CaseNote {
        val offender = offenderRepository.findByNomsIdAndSoftDeletedIsFalse(header.nomisId)
            ?: throw OffenderNotFoundException(header.nomisId)

        val relatedIds = relatedService.findRelatedCaseNoteIds(offender.id, body.typeLookup())

        val defaultType = lazy { caseNoteTypeRepository.getByCode(CaseNoteType.DEFAULT_CODE) }
        val caseNoteType =
            if (featureFlags.defaultTypeForNonNsi() && !body.typeLookup().isAlertType() && relatedIds.nsiId == null) {
                defaultType.value
            } else {
                nomisTypeRepository.findByIdOrNull(body.typeLookup())?.type ?: defaultType.value
            }

        val description = if (featureFlags.setDescription()) body.description(caseNoteType) else null

        val assignment = assignmentService.findAssignment(body.establishmentCode, body.staffName)

        return CaseNote(
            offenderId = offender.id,
            eventId = relatedIds.eventId,
            nsiId = relatedIds.nsiId,
            type = caseNoteType,
            nomisId = header.noteId,
            description = description,
            notes = body.notes(),
            date = body.contactTimeStamp,
            startTime = body.contactTimeStamp,
            isSensitive = caseNoteType.isSensitive,
            probationAreaId = assignment.first,
            teamId = assignment.second,
            staffId = assignment.third,
            staffEmployeeId = assignment.third
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DeliusService::class.java)
    }
}
