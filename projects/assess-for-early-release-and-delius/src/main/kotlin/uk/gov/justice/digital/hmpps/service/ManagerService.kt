package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.entity.Caseload
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class ManagerService(
    private val ldapTemplate: LdapTemplate,
    private val personManagerRepository: PersonManagerRepository
) {
    fun findCommunityManager(crn: String): Manager =
        personManagerRepository.findByPersonCrn(crn)?.let { ro ->
            ro.staff.user?.apply {
                email = ldapTemplate.findEmailByUsername(username)
            }
            ro.asManager()
        } ?: throw NotFoundException("CommunityManager", "crn", crn)

    fun findCommunityManagerEmails(crns: List<String>): List<StaffEmail> =
        personManagerRepository.findByPersonCrnIn(crns).map {
            StaffEmail(it.staff.code, it.staff.user?.username?.let { ldapTemplate.findEmailByUsername(it) })
        }
}

fun PersonManager.asManager() = Manager(
    staff.id,
    staff.code,
    staff.name(),
    provider.asProvider(),
    team.asTeam(),
    staff.user?.username,
    staff.user?.email,
    staff.isUnallocated()
)

fun Staff.name() = Name(forename, middleName, surname)
fun uk.gov.justice.digital.hmpps.entity.Provider.asProvider() =
    uk.gov.justice.digital.hmpps.api.model.Provider(code, description)

fun uk.gov.justice.digital.hmpps.entity.Team.asTeam() = uk.gov.justice.digital.hmpps.api.model.Team(
    code,
    description,
    telephone,
    emailAddress,
    addresses.mapNotNull(uk.gov.justice.digital.hmpps.entity.OfficeLocation::asTeamAddress),
    district.asDistrict(),
    district.borough.asBorough(),
    startDate,
    endDate
)

fun uk.gov.justice.digital.hmpps.entity.OfficeLocation.asTeamAddress() = OfficeAddress.from(
    description,
    buildingName,
    buildingNumber,
    streetName,
    district,
    townCity,
    county,
    postcode,
    ldu.description,
    telephoneNumber,
    startDate,
    endDate
)

fun Caseload.asManagedOffender() = ManagedOffender(
    crn,
    Name(firstName, secondName, surname),
    allocationDate,
    staff.asStaff(),
    team.asTeam()
)

fun uk.gov.justice.digital.hmpps.entity.District.asDistrict() =
    uk.gov.justice.digital.hmpps.api.model.District(code, description, borough.asBorough())

fun uk.gov.justice.digital.hmpps.entity.Borough.asBorough() =
    uk.gov.justice.digital.hmpps.api.model.Borough(code, description)
