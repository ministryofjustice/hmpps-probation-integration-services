package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.LocalAdminUnit
import uk.gov.justice.digital.hmpps.model.LocalAdminUnitWithTeams
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.ProviderWithLaus
import uk.gov.justice.digital.hmpps.model.Team
import uk.gov.justice.digital.hmpps.repository.DistrictRepository
import uk.gov.justice.digital.hmpps.repository.ProbationAreaRepository

@RestController
@RequestMapping(value = ["/providers"])
@PreAuthorize("hasRole('PROBATION_API__HDC__STAFF')")
class ProviderController(
    private val probationAreaRepository: ProbationAreaRepository,
    private val districtRepository: DistrictRepository,
) {
    @GetMapping
    fun getProviders() = probationAreaRepository.findSelectableProbationAreas().map { Provider(it.code, it.description) }

    @GetMapping("/{code}")
    fun getProvider(
        @PathVariable code: String,
    ) = probationAreaRepository.findByCodeWithSelectableDistricts(code)
        ?.let { probationArea ->
            ProviderWithLaus(
                code = probationArea.code,
                description = probationArea.description,
                localAdminUnits = probationArea.boroughs.flatMap { it.districts }.map { LocalAdminUnit(it.code, it.description) },
            )
        } ?: throw NotFoundException("Provider", "code", code)

    @GetMapping("/{providerCode}/localAdminUnits/{lauCode}")
    fun getLocalAdminUnit(
        @PathVariable providerCode: String,
        @PathVariable lauCode: String,
    ) = districtRepository.findByProbationAreaAndCode(providerCode, lauCode)
        ?.let { district ->
            LocalAdminUnitWithTeams(
                code = district.code,
                description = district.description,
                teams = district.teams.map { Team(it.code, it.description) },
            )
        } ?: throw NotFoundException("Local Admin Unit not found for provider '$providerCode' and code '$lauCode'")
}
