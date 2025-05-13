package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.asAddress
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import uk.gov.justice.digital.hmpps.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.repository.ProbationDeliveryUnitRepository
import uk.gov.justice.digital.hmpps.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.repository.TeamRepository

@Service
class ProviderService(
    private val providerRepository: ProviderRepository,
    private val probationDeliveryUnitRepository: ProbationDeliveryUnitRepository,
    private val teamRepository: TeamRepository,
) {

    fun getProviders(): List<Provider> {
        return providerRepository.findSelectableProviders().map { it.toProvider() }
    }

    fun getPdus(providerCode: String): List<ProbationDeliveryUnit> {
        return probationDeliveryUnitRepository.findPdus(providerCode).map { it.toPdu() }
    }

    fun getPduLocations(providerCode: String, pduCode: String): List<OfficeAddress> {
        return teamRepository.getTeams(providerCode, pduCode)
            .flatMap { team -> team.addresses.mapNotNull { it.asAddress(team.emailAddress) } }
    }
}

fun uk.gov.justice.digital.hmpps.entity.Provider.toProvider() = Provider(
    code = code.trim(),
    description = description,
)

fun uk.gov.justice.digital.hmpps.entity.Borough.toPdu() = ProbationDeliveryUnit(
    code = code.trim(),
    description = description,
)
