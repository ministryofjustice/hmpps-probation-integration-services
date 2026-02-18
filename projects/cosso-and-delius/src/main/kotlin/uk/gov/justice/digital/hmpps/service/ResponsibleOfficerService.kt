package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.entity.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.entity.UserRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import uk.gov.justice.digital.hmpps.model.ResponsibleOfficerDetails

@Service
class ResponsibleOfficerService(
    val documentRepository: DocumentRepository,
    val responsibleOfficerRepository: ResponsibleOfficerRepository,
    val ldapTemplate: LdapTemplate,
    val officeLocationRepository: OfficeLocationRepository,
    private val userRepository: UserRepository,

) {
    fun getResponsibleOfficerDetails(crn: String): ResponsibleOfficerDetails {
        val responsibleOfficer = responsibleOfficerRepository.findByPerson_Crn(crn) ?: throw NotFoundException(
            "ResponsibleOfficer", "crn", crn
        )
        val staffId = responsibleOfficer.offenderManager?.staff?.id ?: responsibleOfficer.prisonOffenderManager?.staff?.id
        val username = userRepository.findByStaffId(staffId!!)?.username
        val staff = responsibleOfficer.offenderManager?.staff ?: responsibleOfficer.prisonOffenderManager?.staff
        val probationArea = responsibleOfficer.offenderManager?.probationArea ?: responsibleOfficer.prisonOffenderManager?.probationArea
        val emailAddress = ldapTemplate.findAttributeByUsername(username!!, "mail" )
        val telephoneNumber = ldapTemplate.findAttributeByUsername(username, "telephoneNumber")
        val officeLocationId = ldapTemplate.findPreferenceByUsername(username, "replyAddress")
        val officeAddress = officeLocationRepository.findById(officeLocationId!!.toLong()).get()

        return ResponsibleOfficerDetails(
            name = Name(
                forename = staff!!.forename,
                middleName = staff.middleName,
                surname = staff.surname
            ),
            emailAddress = emailAddress!!,
            telephoneNumber = telephoneNumber!!,
            replyAddress = OfficeAddress(
                id = officeAddress.id,
                officeDescription = officeAddress.description,
                buildingName = officeAddress.buildingName,
                buildingNumber = officeAddress.buildingNumber,
                streetName = officeAddress.streetName,
                townCity = officeAddress.townCity,
                county = officeAddress.county,
                district = officeAddress.district,
                postcode = officeAddress.postcode,
            ),
            probationArea = CodeAndDescription(
                code = probationArea!!.code,
                description = probationArea.description
            )
        )
    }
}