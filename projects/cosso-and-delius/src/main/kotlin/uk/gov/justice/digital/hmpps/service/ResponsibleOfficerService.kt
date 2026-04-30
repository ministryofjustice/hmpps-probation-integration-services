package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import uk.gov.justice.digital.hmpps.model.ResponsibleOfficerDetails

@Service
class ResponsibleOfficerService(
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val ldapTemplate: LdapTemplate,
    private val officeLocationRepository: OfficeLocationRepository,
    private val userRepository: UserRepository,
) {
    fun getResponsibleOfficerDetails(crn: String): ResponsibleOfficerDetails {
        val responsibleOfficer = responsibleOfficerRepository.findByPersonCrn(crn).orNotFoundBy("CRN", crn)
        val username = userRepository.findByStaffId(responsibleOfficer.staff.id)?.username
        val emailAddress = username?.let { ldapTemplate.findAttributeByUsername(username, "mail") }
        val telephoneNumber = username?.let { ldapTemplate.findAttributeByUsername(username, "telephoneNumber") }
        val officeLocation = username
            ?.let { ldapTemplate.findPreferenceByUsername(username, "replyAddress") }?.toLongOrNull()
            ?.let { officeLocationRepository.findByIdOrNull(it) }

        return ResponsibleOfficerDetails(
            name = with(responsibleOfficer.staff) { Name(forename, middleName, surname) },
            emailAddress = emailAddress,
            telephoneNumber = telephoneNumber,
            replyAddress = officeLocation?.toAddress(),
            probationArea = with(responsibleOfficer.probationArea) { CodeAndDescription(code, description) }
        )
    }

    private val ResponsibleOfficer.staff
        get() = checkNotNull(offenderManager?.staff ?: prisonOffenderManager?.staff)
    private val ResponsibleOfficer.probationArea
        get() = checkNotNull(offenderManager?.probationArea ?: prisonOffenderManager?.probationArea)

    private fun OfficeLocation.toAddress() = OfficeAddress(
        id = id,
        officeDescription = description,
        buildingName = buildingName,
        buildingNumber = buildingNumber,
        streetName = streetName,
        townCity = townCity,
        county = county,
        district = district,
        postcode = postcode
    )
}
