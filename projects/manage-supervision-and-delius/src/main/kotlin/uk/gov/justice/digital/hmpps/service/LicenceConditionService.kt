package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import kotlin.jvm.optionals.getOrNull
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition as EntityLicenceCondition

@Service
class LicenceConditionService(
    private val personRepository: PersonRepository,
    private val licenceConditionRepository: LicenceConditionRepository
) {

    fun getLicenceConditionNote(crn: String, licenceConditionId: Long, noteId: Int): LicenceConditionNoteDetail {
        val person = personRepository.getPerson(crn)

        val licenceCondition = licenceConditionRepository.findById(licenceConditionId).getOrNull()

        return LicenceConditionNoteDetail(
            person.toSummary(),
            licenceCondition?.toLicenceConditionSingleNote(noteId, false)
        )
    }
}

fun EntityLicenceCondition.toLicenceCondition() =
    LicenceCondition(
        id,
        mainCategory.description,
        subCategory?.description,
        imposedReleasedDate,
        actualStartDate,
        toLicenceConditionNote(true)
    )

fun EntityLicenceCondition.toMinimalLicenceCondition() = MinimalLicenceCondition(id, mainCategory.description)

fun EntityLicenceCondition.toLicenceConditionSingleNote(noteId: Int, truncateNote: Boolean) =
    LicenceCondition(
        id,
        mainCategory.description,
        subCategory?.description,
        imposedReleasedDate,
        actualStartDate,
        licenceConditionNote = toLicenceConditionNote(truncateNote).elementAtOrNull(noteId)
    )

fun EntityLicenceCondition.toLicenceConditionNote(truncateNote: Boolean): List<NoteDetail> {

    return formatNote(notes, truncateNote)
}
