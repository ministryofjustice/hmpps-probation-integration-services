package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.LdapUser
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class ManagerService(
    private val ldapTemplate: LdapTemplate,
    private val personManagerRepository: PersonManagerRepository
) {
    fun findCommunityManager(crnOrNomisId: String): Manager =
        personManagerRepository.findByPersonCrnOrPersonNomsNumber(crnOrNomisId)?.withLdapDetails()?.asManager()
            ?: throw NotFoundException("CommunityManager", "CRN or NOMIS id", crnOrNomisId)

    fun findCommunityManagers(crnsOrNomisIds: List<String>): List<Manager> =
        personManagerRepository.findByPersonCrnInOrPersonNomsNumberIn(crnsOrNomisIds)
            .map { it.withLdapDetails().asManager() }

    private fun PersonManager.withLdapDetails() = apply {
        staff.user?.apply {
            val ldapUser = ldapTemplate.findByUsername<LdapUser>(username)
            email = ldapUser?.email
            telephoneNumber = ldapUser?.telephoneNumber
        }
    }
}

fun PersonManager.asManager() = Manager(
    id = staff.id,
    case = ProbationCase(person.crn, person.nomsNumber, person.pncNumber, person.croNumber),
    code = staff.code,
    name = staff.name(),
    provider = provider.asProvider(),
    team = team.asTeam(),
    username = staff.user?.username,
    email = staff.user?.email,
    telephoneNumber = staff.user?.telephoneNumber,
    unallocated = staff.isUnallocated(),
    allocationDate = allocationDate,
)

fun Staff.name() = Name(forename, middleName, surname)
fun Provider.asProvider() = uk.gov.justice.digital.hmpps.api.model.Provider(code, description)
fun Team.asTeam() = uk.gov.justice.digital.hmpps.api.model.Team(
    code,
    description,
    telephone,
    emailAddress,
    addresses.mapNotNull(OfficeLocation::asTeamAddress),
    district.asDistrict(),
    district.borough.asBorough(),
    startDate,
    endDate
)

fun OfficeLocation.asTeamAddress() = OfficeAddress.from(
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

fun uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.Caseload.asManagedOffender() = ManagedOffender(
    crn,
    person.nomsNumber,
    Name(firstName, secondName, surname),
    allocationDate,
    staff.asStaff(),
    team.asTeam()
)

fun District.asDistrict() = uk.gov.justice.digital.hmpps.api.model.District(code, description, borough.asBorough())
fun Borough.asBorough() = uk.gov.justice.digital.hmpps.api.model.Borough(code, description)
