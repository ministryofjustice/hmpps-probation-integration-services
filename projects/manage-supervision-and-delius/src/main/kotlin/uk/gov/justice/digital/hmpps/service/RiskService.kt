package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlag
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlags
import uk.gov.justice.digital.hmpps.api.model.risk.RiskFlag
import uk.gov.justice.digital.hmpps.api.model.risk.RiskFlagRemoval
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getSummary
import uk.gov.justice.digital.hmpps.integrations.delius.risk.DeRegistration
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlagRepository
import uk.gov.justice.digital.hmpps.integrations.delius.risk.getRiskFlag

@Service
class RiskService(
    private val personRepository: PersonRepository,
    private val riskFlagRepository: RiskFlagRepository
) {

    @Transactional
    fun getPersonRiskFlag(crn: String, riskFlagId: Long): PersonRiskFlag {
        val summary = personRepository.getSummary(crn)
        val riskFlag = riskFlagRepository.getRiskFlag(summary.id, riskFlagId)
        return PersonRiskFlag(
            personSummary = summary.toPersonSummary(),
            riskFlag = riskFlag.toRiskFlag()
        )
    }

    @Transactional
    fun getPersonRiskFlags(crn: String): PersonRiskFlags {
        val summary = personRepository.getSummary(crn)
        val riskFlags = riskFlagRepository.findByPersonId(summary.id)
        return PersonRiskFlags(
            personSummary = summary.toPersonSummary(),
            riskFlags = riskFlags.filter { !it.deRegistered }.map { it.toRiskFlag() },
            removedRiskFlags = riskFlags.filter { it.deRegistered }.map { it.toRiskFlag() }
        )
    }
}

fun uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlag.toRiskFlag() = RiskFlag(
    id = id,
    description = type.description,
    notes = notes,
    createdDate = createdDate,
    createdBy = Name(forename = createdBy.forename, surname = createdBy.surname),
    nextReviewDate = nextReviewDate,
    mostRecentReviewDate = reviews.filter { it.completed == true }.maxByOrNull { it.date }?.date,
    removed = deRegistered,
    removalHistory = deRegistrations.sortedByDescending { it.deRegistrationDate }.map { it.toRiskFlagRemoval() }
)

fun DeRegistration.toRiskFlagRemoval() = RiskFlagRemoval(
    notes = notes,
    removedBy = Name(forename = staff.forename, surname = staff.surname),
    removalDate = deRegistrationDate
)