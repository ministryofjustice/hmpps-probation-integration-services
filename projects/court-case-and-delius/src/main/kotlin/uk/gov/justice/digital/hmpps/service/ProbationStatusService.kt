package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ManagedStatus
import uk.gov.justice.digital.hmpps.api.model.ProbationStatusDetail
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.SentenceCounts

@Service
class ProbationStatusService(private val personRepository: PersonRepository) {
    fun getProbationStatus(crn: String): ProbationStatusDetail = personRepository.statusOf(crn).detail()
}

fun SentenceCounts.detail() = ProbationStatusDetail(
    status,
    terminationDate,
    breachCount > 0,
    preSentenceCount > 0,
    awaitingPsrCount > 0
)

val SentenceCounts.status: ManagedStatus
    get() = when {
        unallocatedCount > 0 && allocatedCount == 0 && previousCount == 0 -> ManagedStatus.NEW_TO_PROBATION
        allocatedCount > 0 -> ManagedStatus.CURRENTLY_MANAGED
        previousCount > 0 -> ManagedStatus.PREVIOUSLY_MANAGED
        else -> ManagedStatus.UNKNOWN
    }
