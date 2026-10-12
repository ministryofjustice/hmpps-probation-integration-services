package uk.gov.justice.digital.hmpps.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Case
import uk.gov.justice.digital.hmpps.api.model.CaseType
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@Service
class ActiveCaseService(
    private val personRepository: PersonRepository,
    @Value("\${delius.db.username}") private val dbUsername: String,
) {
    companion object {
        private const val ORACLE_IN_CLAUSE_BATCH_SIZE = 999
        private const val CASE_TYPE_BATCH_SIZE = 50
        private const val SINGLE_GROUP = "single-group"
    }

    fun getCases(crns: List<String>): List<Case> = getCasesByGroup(mapOf(SINGLE_GROUP to crns)).getValue(SINGLE_GROUP)

    fun getCasesByGroup(crnsByGroup: Map<String, List<String>>): Map<String, List<Case>> {
        val distinctCrnsByGroup = crnsByGroup.mapValues { (_, crns) -> crns.distinct() }
        val crnSet = distinctCrnsByGroup.values.flatten().toSet()
        if (crnSet.isEmpty()) {
            return distinctCrnsByGroup.mapValues { emptyList() }
        }

        val initialAllocationDates =
            crnSet.chunkedForOracleInClause()
                .flatMap { personRepository.findMostRecentInitialAllocations(it, dbUsername) }
                .associate { it.crn to it.allocatedAt?.toLocalDate() }
        val caseTypes = crnSet.chunkedForCaseTypeInClause()
            .flatMap { personRepository.findCaseTypes(it) }
            .associate { it.crn to it.type }
        val peopleByCrn = crnSet.chunkedForOracleInClause()
            .flatMap { personRepository.findAllByCrnAndSoftDeletedFalse(it.toList()) }
            .associateBy { it.crn }

        return distinctCrnsByGroup.mapValues { (_, crns) ->
            crns.mapNotNull { crn ->
                peopleByCrn[crn]?.let {
                    Case(
                        crn,
                        it.name(),
                        caseTypes[crn] ?: CaseType.UNKNOWN.name,
                        initialAllocationDates[crn]
                    )
                }
            }
        }
    }

    private fun Set<String>.chunkedForOracleInClause() =
        toList().chunked(ORACLE_IN_CLAUSE_BATCH_SIZE).map { it.toSet() }

    private fun Set<String>.chunkedForCaseTypeInClause() =
        toList().chunked(CASE_TYPE_BATCH_SIZE).map { it.toSet() }
}

