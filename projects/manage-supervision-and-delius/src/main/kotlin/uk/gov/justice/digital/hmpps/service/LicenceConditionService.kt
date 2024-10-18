package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.LicenceConditionNote
import uk.gov.justice.digital.hmpps.api.model.sentence.LicenceConditionNoteDetail
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition as EntityLicenceCondition

@Service
class LicenceConditionService(
    private val personRepository: PersonRepository,
    private val licenceConditionRepository: LicenceConditionRepository) {

    fun getLicenceConditionNote(crn: String, licenceConditionId: Long, noteId: Int): LicenceConditionNoteDetail {
        val person = personRepository.getPerson(crn)

        val licenceCondition = licenceConditionRepository.findById(licenceConditionId).getOrNull()

        return LicenceConditionNoteDetail(
            person.toSummary(),
            licenceCondition?.toLicenceConditionNote(false)?.let {
                when {
                    it.size > noteId -> it[noteId]
                    else -> null
                }
            }
        )

    }
}

fun EntityLicenceCondition.toLicenceConditionNote(truncateNote: Boolean): List<LicenceConditionNote> {

    return  notes?.let {
        val splitParam = "---------------------------------------------------------" + System.lineSeparator()
        notes.split(splitParam).mapIndexed { index, note ->
            val matchResult = Regex(
                "^Comment added by (.+?) on (\\d{2}/\\d{2}/\\d{4}) at \\d{2}:\\d{2}"
                    + System.lineSeparator()
            ).find(note)
            val commentLine = matchResult?.value
            val commentText = commentLine?.let { note.removePrefix(commentLine) } ?: note

            val userCreatedBy = matchResult?.groupValues?.get(1)
            val dateCreatedBy = matchResult?.groupValues?.get(2)
                ?.let { LocalDate.parse(it, DeliusDateFormatter) }


            LicenceConditionNote(
                index,
                userCreatedBy,
                dateCreatedBy,
                when (truncateNote) {
                    true -> commentText.removeSuffix(System.lineSeparator()).chunked(1500)[0]
                    else -> commentText
                },
                when (truncateNote) {
                    true -> note.length > 1500
                    else -> null
                }
            )
        }
    } ?: listOf()
}
