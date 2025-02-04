package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.api.model.sentence.Requirement
import uk.gov.justice.digital.hmpps.api.model.sentence.RequirementNoteDetail
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Requirement as RequirementEntity

@Service
class RequirementService(
    private val personRepository: PersonRepository,
    private val requirementRepository: RequirementRepository
) {
    fun getRequirementNote(crn: String, requirementId: Long, noteId: Int): RequirementNoteDetail {
        val person = personRepository.getPerson(crn)

        val requirement = requirementRepository.getRequirement(requirementId)

        return RequirementNoteDetail(
            person.toSummary(),
            requirement?.toRequirementSingleNote(noteId)
        )
    }

    fun RequirementEntity.toRequirementSingleNote(noteId: Int): Requirement {
        val rar = getRar(disposal!!.id, mainCategory!!.code)

        val requirement = Requirement(
            id,
            mainCategory.code,
            expectedStartDate,
            startDate,
            expectedEndDate,
            terminationDate,
            terminationDetails?.description,
            populateRequirementDescription(mainCategory.description, subCategory?.description, length, rar),
            length,
            mainCategory.unitDetails?.description,
            requirementNote = toRequirementNote(false).elementAtOrNull(noteId),
            rar = rar
        )

        return requirement
    }

    fun getRar(disposalId: Long, requirementType: String): Rar? {
        if (requirementType.equals("F", true)) {
            val rarDays = requirementRepository.getRarDaysByDisposalId(disposalId)
            val scheduledDays = rarDays.find { it.type == "SCHEDULED" }?.days ?: 0
            val completedDays = rarDays.find { it.type == "COMPLETED" }?.days ?: 0
            val nsiCompletedDays = rarDays.find { it.type == "NSI_COMPLETED" }?.days ?: 0
            return Rar(completed = completedDays, nsiCompleted = nsiCompletedDays, scheduled = scheduledDays)
        }

        return null
    }

    fun getRarDescription(eventId: Long, eventNumber: String, disposalId: Long): String? {
        val rarCode = "F"
        val rarRequirements = requirementRepository.getRequirements(eventId, eventNumber)
            .filter { it.mainCategory!!.code == rarCode }


        if (rarRequirements.isNotEmpty()) {
            val rar = getRar(disposalId, rarCode)

            return rar?.let { r -> "${r.totalDays} of ${rarRequirements.sumOf { it.length!! }} RAR days completed" }
        }

        return null
    }
}

fun populateRequirementDescription(
    description: String,
    codeDescription: String?,
    requirementLength: Long?,
    rar: Rar?
): String {
    rar?.let { return "${it.totalDays} of $requirementLength RAR days completed" }

    if (codeDescription != null) {
        return "$description - $codeDescription"
    }

    return description
}

fun RequirementEntity.toRequirementNote(truncateNote: Boolean): List<NoteDetail> {
    return formatNote(notes, truncateNote)
}