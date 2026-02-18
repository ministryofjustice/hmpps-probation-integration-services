package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.OffenderManager
import uk.gov.justice.digital.hmpps.entity.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.entity.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.entity.ProbationArea
import uk.gov.justice.digital.hmpps.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.entity.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.UserRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import uk.gov.justice.digital.hmpps.model.ResponsibleOfficerDetails
import kotlin.jvm.optionals.getOrNull

@Service
class ResponsibleOfficerService(
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val ldapTemplate: LdapTemplate,
    private val officeLocationRepository: OfficeLocationRepository,
    private val userRepository: UserRepository,

    ) {
    fun getResponsibleOfficerDetails(crn: String): ResponsibleOfficerDetails {
        val responsibleOfficer = responsibleOfficerRepository.findByPerson_Crn(crn) ?: throw NotFoundException(
            "ResponsibleOfficer", "crn", crn
        )
        val offenderManager = responsibleOfficer.offenderManager
        val prisonOffenderManager = responsibleOfficer.prisonOffenderManager
        val staff = responsibleOfficer.getStaff()
        val username =
            userRepository.findByStaffId(staff.id)?.username ?: throw NotFoundException("User", "staffId", staff.id)
        val probationArea =
            getProbationAreaForResponsibleOfficer(offenderManager, prisonOffenderManager, responsibleOfficer)
        val emailAddress = ldapTemplate.findAttributeByUsername(username, "mail")
        val telephoneNumber = ldapTemplate.findAttributeByUsername(username, "telephoneNumber")

        return ResponsibleOfficerDetails(
            name = Name(
                forename = staff.forename,
                middleName = staff.middleName,
                surname = staff.surname
            ),
            emailAddress = emailAddress,
            telephoneNumber = telephoneNumber,
            replyAddress = officeAddress(username),
            probationArea = CodeAndDescription(
                code = probationArea.code,
                description = probationArea.description
            )
        )
    }

    private fun ResponsibleOfficer.getStaff() =
        (offenderManager?.staff ?: prisonOffenderManager?.staff).orNotFoundBy("Staff", "responsibleOfficerId")

    private fun getProbationAreaForResponsibleOfficer(
        offenderManager: OffenderManager?,
        prisonOffenderManager: PrisonOffenderManager?,
        responsibleOfficer: ResponsibleOfficer
    ): ProbationArea {
        val probationArea = offenderManager?.probationArea ?: prisonOffenderManager?.probationArea
        if (probationArea == null) throw NotFoundException(
            "ProbationArea",
            "responsibleOfficerId",
            responsibleOfficer.id
        )
        return probationArea
    }

    private fun officeAddress(username: String): OfficeAddress? =
        ldapTemplate.findPreferenceByUsername(username, "replyAddress")
            ?.toLongOrNull()
            ?.let { officeLocationRepository.findById(it).getOrNull() }
            ?.let { officeLocation ->
                OfficeAddress(
                    id = officeLocation.id,
                    officeDescription = officeLocation.description,
                    buildingName = officeLocation.buildingName,
                    buildingNumber = officeLocation.buildingNumber,
                    streetName = officeLocation.streetName,
                    townCity = officeLocation.townCity,
                    county = officeLocation.county,
                    district = officeLocation.district,
                    postcode = officeLocation.postcode
                )
            }
}