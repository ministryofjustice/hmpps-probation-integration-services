package uk.gov.justice.digital.hmpps.integrations.delius.service

import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction.Outcome.SUCCESS
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.getByCode
import uk.gov.justice.digital.hmpps.datetime.toDeliusDateTime
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.CASE_NOTES_MERGE
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader.Type.*
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Action.Created
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Action.Updated
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Success
import uk.gov.justice.digital.hmpps.integrations.delius.repository.*
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.service.AssignmentService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class DeliusService(
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val auditedInteractionRepository: AuditedInteractionRepository,
    private val caseNoteRepository: CaseNoteRepository,
    private val nomisTypeRepository: CaseNoteNomisTypeRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val offenderRepository: OffenderRepository,
    private val assignmentService: AssignmentService,
    private val relatedService: CaseNoteRelatedService,
    private val offenderManagerRepository: OffenderManagerRepository,
) {
    @Transactional
    fun mergeCaseNote(@Valid caseNote: DeliusCaseNote): Success? {
        val (existing, action) = when (caseNote.header.type) {
            CaseNote -> caseNoteRepository.findByExternalReference(caseNote.urn)
                ?: caseNote.header.legacyId?.let { caseNoteRepository.findByNomisId(it) }

            ActiveAlert, InactiveAlert -> caseNoteRepository.findByExternalReference(caseNote.urn)
        }?.let { cn ->
            if (cn.offender.nomsId == caseNote.header.nomisId) cn to Updated
            else {
                val new = caseNote.newEntity().copy(id = cn.id, version = cn.version)
                val action = MergeResult.Action.Moved(cn.offender.crn, new.offender.crn)
                caseNoteRepository.save(new) to action
            }
        } ?: (null to Created)

        return (if (existing == null) caseNote.newEntity() else existing.updateFrom(
            caseNote,
            action is MergeResult.Action.Moved
        ))?.let(caseNoteRepository::save)
            ?.also {
                auditCaseNoteMerge(
                    AuditedInteraction.Parameters(
                        "dpsId" to caseNote.header.uuid.toString(),
                        "contactId" to it.id.toString()
                    )
                )
            }
            ?.let { Success(it.offender.crn, it.offender.nomsId!!, action) }
    }

    @Transactional
    fun createDataCleanseContact(movedNotes: Set<String>) {
        movedNotes.mapNotNull { dataCleanseContact(it) }
            .takeIf { it.isNotEmpty() }
            ?.also(caseNoteRepository::saveAll)
    }

    private fun CaseNote.updateFrom(caseNote: DeliusCaseNote, hasMoved: Boolean): CaseNote? {
        val last = lastModifiedDateTime.truncatedTo(ChronoUnit.SECONDS)
        val current = caseNote.body.systemTimestamp.truncatedTo(ChronoUnit.SECONDS)
        return when {
            hasMoved -> this
            last.isBefore(current) -> copy(
                notes = caseNote.body.notes(notes.length),
                date = caseNote.body.contactTimeStamp,
                startTime = caseNote.body.contactTimeStamp,
                lastModifiedDateTime = caseNote.body.systemTimestamp,
                externalReference = caseNote.urn
            )

            externalReference == null && caseNote.isOfInterestForExternalReference() -> apply {
                externalReference = caseNote.urn
            }

            else -> {
                log.warn("Case Note update ignored because it was out of sequence ${caseNote.header}")
                null
            }
        }
    }

    private fun auditCaseNoteMerge(params: AuditedInteraction.Parameters) {
        val bi = businessInteractionRepository.getByCode(CASE_NOTES_MERGE.code)
        auditedInteractionRepository.save(
            AuditedInteraction(
                businessInteractionId = bi.id,
                userId = ServiceContext.servicePrincipal()!!.userId,
                dateTime = ZonedDateTime.now(),
                parameters = params,
                outcome = SUCCESS
            )
        )
    }

    private fun DeliusCaseNote.newEntity(): CaseNote {
        val offender = offenderRepository.getByNomsId(header.nomisId)

        val relatedIds = relatedService.findRelatedCaseNoteIds(offender.id, body.typeLookup())

        val caseNoteType = nomisTypeRepository.findByIdOrNull(body.typeLookup())?.type
            ?: caseNoteTypeRepository.getByCode(CaseNoteType.DEFAULT_CODE)

        val description = body.description(caseNoteType)

        val assignment = assignmentService.findAssignment(body.establishmentCode, body.staffName)

        return CaseNote(
            offender = offender,
            eventId = relatedIds.eventId,
            nsiId = relatedIds.nsiId,
            type = caseNoteType,
            description = description,
            notes = body.notes(),
            date = body.contactTimeStamp,
            startTime = body.contactTimeStamp,
            isSensitive = caseNoteType.isSensitive,
            probationAreaId = assignment.first,
            teamId = assignment.second,
            staffId = assignment.third,
            staffEmployeeId = assignment.third,
            externalReference = urn,
            createdDateTime = body.systemTimestamp,
            lastModifiedDateTime = body.systemTimestamp,
        )
    }

    private fun dataCleanseContact(crn: String): CaseNote? =
        offenderManagerRepository.findByOffenderCrn(crn)?.let { om ->
            val caseNoteType = caseNoteTypeRepository.getByCode(CaseNoteType.OTHER_INFORMATION)
            val now = ZonedDateTime.now()
            return CaseNote(
                offender = om.offender,
                eventId = null,
                nsiId = null,
                type = caseNoteType,
                description = "Data Cleanse",
                notes = "On ${now.toDeliusDateTime()} case notes from Prison associated with a NOMS number that had been mistakenly associated with this case record were removed.",
                date = now,
                startTime = now,
                isSensitive = caseNoteType.isSensitive,
                probationAreaId = om.probationArea.id,
                teamId = om.team.id,
                staffId = om.staff.id,
                staffEmployeeId = om.staff.id,
                externalReference = null,
                createdDateTime = now,
                lastModifiedDateTime = now,
            )
        }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DeliusService::class.java)
    }
}
