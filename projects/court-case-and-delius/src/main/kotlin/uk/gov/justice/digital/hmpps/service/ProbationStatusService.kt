package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Ids
import uk.gov.justice.digital.hmpps.api.model.ProbationCase
import uk.gov.justice.digital.hmpps.api.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.model.ProbationStatusDetail
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.SentenceCounts

@Service
class ProbationStatusService(private val personRepository: PersonRepository) {
    fun getProbationStatus(crn: String): ProbationStatusDetail =
        personRepository.statusOf(crn)?.detail() ?: ProbationStatusDetail.NO_RECORD

    fun getProbationCase(crn: String): ProbationCase =
        personRepository.findByCrnAndSoftDeletedIsFalse(crn)?.asProbationCase()
            ?: throw NotFoundException("Person", "crn", crn)

    private fun Person.asProbationCase() =
        ProbationCase(id, Ids(crn, nomsNumber, pnc), personRepository.statusOf(crn)!!.detail())
}

fun SentenceCounts.detail() = ProbationStatusDetail(
    status,
    terminationDate,
    breachCount > 0,
    preSentenceCount > 0,
    awaitingPsrCount > 0
)

val SentenceCounts.status: ProbationStatus
    get() = when {
        currentCount > 0 -> ProbationStatus.CURRENT
        previousCount > 0 -> ProbationStatus.PREVIOUSLY_KNOWN
        else -> ProbationStatus.NOT_SENTENCED
    }
