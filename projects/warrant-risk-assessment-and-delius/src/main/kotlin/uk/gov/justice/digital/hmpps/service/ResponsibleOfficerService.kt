package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.*

@Service
class ResponsibleOfficerService(
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val ldapTemplate: LdapTemplate,
    private val officeLocationRepository: OfficeLocationRepository,
    private val userRepository: UserRepository,
) {
    fun getResponsibleOfficerDetails(crn: String): ResponsibleOfficerDetails {
        val responsibleOfficer = responsibleOfficerRepository.findByPersonCrn(crn)
            ?: throw NotFoundException("ResponsibleOfficer", "crn", crn)

        val staff = checkNotNull(
            responsibleOfficer.offenderManager?.staff ?: responsibleOfficer.prisonOffenderManager?.staff
        )
        val probationArea = checkNotNull(
            responsibleOfficer.offenderManager?.probationArea ?: responsibleOfficer.prisonOffenderManager?.probationArea
        )

        val username = userRepository.findByStaffId(staff.id)?.username
        val telephoneNumber = username?.let { ldapTemplate.findAttributeByUsername(it, "telephoneNumber") }
        val homeArea = username?.let { ldapTemplate.findAttributeByUsername(it, "userHomeArea") }
        val defaultReplyAddressId =
            username?.let { ldapTemplate.findPreferenceByUsername(it, "replyAddress")?.toLongOrNull() }

        val officeLocations = homeArea?.let { officeLocationRepository.findAllByProbationAreaCode(it) }

        // Find the default reply address (the one matching the user's preference)
        val replyAddress = officeLocations
            ?.firstOrNull { it.id == defaultReplyAddressId }
            ?.toOfficeAddress(status = "Postal")
            ?: officeLocations?.firstOrNull()?.toOfficeAddress(status = null)

        return ResponsibleOfficerDetails(
            name = Name(
                forename = staff.forename,
                middleName = staff.middleName,
                surname = staff.surname,
            ),
            telephoneNumber = telephoneNumber,
            probationArea = CodeAndDescription(
                code = probationArea.code,
                description = probationArea.description,
            ),
            replyAddress = replyAddress,
        )
    }

    private fun OfficeLocation.toOfficeAddress(status: String?) = OfficeAddress(
        id = id,
        status = status,
        officeDescription = description,
        buildingName = buildingName,
        buildingNumber = buildingNumber,
        streetName = streetName,
        townCity = townCity,
        district = district,
        county = county,
        postcode = postcode,
    )
}
