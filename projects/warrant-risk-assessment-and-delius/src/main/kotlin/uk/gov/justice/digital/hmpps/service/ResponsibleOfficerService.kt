package uk.gov.justice.digital.hmpps.service

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
import uk.gov.justice.digital.hmpps.model.UserDetailsName

@Service
class ResponsibleOfficerService(
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val ldapTemplate: LdapTemplate,
    private val officeLocationRepository: OfficeLocationRepository,
) {
    fun getResponsibleOfficerDetails(crn: String, username: String): ResponsibleOfficerDetails {
        val responsibleOfficer = responsibleOfficerRepository.findByPersonCrn(crn).orNotFoundBy("CRN", crn)
        val submittingUsersFirstName =
            ldapTemplate.findAttributeByUsername(username, "givenname").orNotFoundBy("Username", username)
        val submittingUsersSurname =
            ldapTemplate.findAttributeByUsername(username, "sn").orNotFoundBy("Username", username)
        val usernameRO = responsibleOfficer.username
        val emailAddress = usernameRO?.let { ldapTemplate.findAttributeByUsername(it, "mail") }
        val telephoneNumber = usernameRO?.let { ldapTemplate.findAttributeByUsername(it, "telephoneNumber") }
        val homeArea = usernameRO?.let { ldapTemplate.findAttributeByUsername(it, "userHomeArea") }
        val defaultReplyAddress =
            usernameRO?.let { ldapTemplate.findPreferenceByUsername(it, "replyAddress")?.toLongOrNull() }
        val officeLocations = homeArea?.let { officeLocationRepository.findAllByProbationAreaCode(it) }

        return ResponsibleOfficerDetails(
            userDetails = UserDetailsName(submittingUsersFirstName, submittingUsersSurname),
            responsibleOfficer = uk.gov.justice.digital.hmpps.model.ResponsibleOfficer(
                name = with(responsibleOfficer.staff) { Name(forename, middleName, surname) },
                emailAddress = emailAddress,
                telephoneNumber = telephoneNumber,
                replyAddresses = officeLocations?.map {
                    it.toAddress().copy(status = if (it.id == defaultReplyAddress) "Default" else null)
                } ?: emptyList(),
                probationArea = with(responsibleOfficer.probationArea) { CodeAndDescription(code, description) }
            )
        )
    }

    private val ResponsibleOfficer.probationArea
        get() = checkNotNull(offenderManager?.probationArea ?: prisonOffenderManager?.probationArea)

    private fun OfficeLocation.toAddress() = OfficeAddress(
        id = id,
        status = null,
        officeDescription = description,
        buildingName = buildingName,
        buildingNumber = buildingNumber,
        streetName = streetName,
        townCity = townCity,
        county = county,
        district = district,
        postcode = postcode,
    )
}
