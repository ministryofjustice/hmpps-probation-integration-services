package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.ldap.core.LdapTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.model.AddressResponse
import uk.gov.justice.digital.hmpps.model.CommunityManagerResponse
import uk.gov.justice.digital.hmpps.repository.AddressRepository
import uk.gov.justice.digital.hmpps.repository.CommunityManagerRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@RestController
@PreAuthorize("hasRole('PROBATION_API__PRISON_EDUCATION__CASE_DETAIL')")
class ApiController(
    private val personRepository: PersonRepository,
    private val communityManagerRepository: CommunityManagerRepository,
    private val addressRepository: AddressRepository,
    private val ldapTemplate: LdapTemplate
) {
    @GetMapping(value = ["/probation-case/{prisonerId}/community-manager"])
    @Operation(
        summary = "Get the current active community manager for a probation case",
        description = """Accepts the prisoner identifier (NOMS number) and returns the 
            currently active community manager of the probation case.
            <p>Requires `PROBATION_API__PRISON_EDUCATION__CASE_DETAIL`.
        """
    )
    fun getCommunityManager(@PathVariable prisonerId: String) = personRepository.findByPrisonerId(prisonerId)
        ?.let { communityManagerRepository.findByPersonId(it.id).staff }
        ?.let {
            CommunityManagerResponse(
                firstName = it.forename,
                lastName = it.surname,
                email = it.user?.username?.let { username -> ldapTemplate.findEmailByUsername(username) }
            )
        }
        ?: throw NotFoundException("Person", "prisonerId", prisonerId)

    @GetMapping(value = ["/probation-case/{prisonerId}/main-address"])
    @Operation(
        summary = "Get the current main address for a probation case",
        description = """Accepts the prisoner identifier (NOMS number) and returns the 
            current main address linked to the probation case.
            <p>Requires `PROBATION_API__PRISON_EDUCATION__CASE_DETAIL`.
        """
    )
    fun getMainAddress(@PathVariable prisonerId: String) = addressRepository.getMainAddressByPrisonerId(prisonerId)
        ?.let {
            AddressResponse(
                buildingName = it.buildingName,
                addressNumber = it.addressNumber,
                streetName = it.streetName,
                district = it.district,
                town = it.townCity,
                county = it.county,
                postcode = it.postcode,
                noFixedAbode = it.noFixedAbode
            )
        }
        ?: throw NotFoundException("Main address", "prisonerId", prisonerId)
}
