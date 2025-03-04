package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.ldap.query.SearchScope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Appointment
import uk.gov.justice.digital.hmpps.api.model.user.*
import uk.gov.justice.digital.hmpps.api.model.user.Staff
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.*
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class UserService(
    private val userRepository: UserRepository,
    private val caseloadRepository: CaseloadRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val userAccessService: UserAccessService,
    private val ldapTemplate: LdapTemplate
) {
    fun getUserDetails(username: String) = ldapTemplate.findByUsername<LdapUser>(username)?.toUserDetails()

    private fun LdapUser.toUserDetails() = userRepository.findUserByUsername(username)?.let { toUserDetails(it.id) }
        ?: throw NotFoundException("User entity", "username", username)

    private fun LdapUser.toUserDetails(userId: Long) = UserDetails(
        userId = userId,
        username = username,
        firstName = forename,
        surname = surname,
        email = email,
        enabled = enabled,
        roles = getUserRoles(dn)
    )

    private fun getUserRoles(name: javax.naming.Name): List<String> = ldapTemplate.search(
        query()
            .base(name)
            .searchScope(SearchScope.ONELEVEL)
            .filter("(|(objectclass=NDRole)(objectclass=NDRoleAssociation))"),
        AttributesMapper { it["cn"].get().toString() }
    )

    @Transactional
    fun getUserCaseload(username: String, pageable: Pageable): StaffCaseload {
        val user = userRepository.getUser(username)
        val caseload = caseloadRepository.findByStaffId(user.staff!!.id, pageable)

        val userAccess = userAccessService.userAccessFor(username, caseload.content.map { it.person.crn })
        return StaffCaseload(
            totalElements = caseload.totalElements.toInt(),
            totalPages = caseload.totalPages,
            provider = user.staff.provider.description,
            caseload = caseload.content.map { it.toStaffCase(userAccess.access.firstOrNull { ua -> ua.crn == it.person.crn }) },
            staff = Name(forename = user.staff.forename, surname = user.staff.surname),
        )
    }

    @Transactional
    fun searchUserCaseload(
        username: String,
        searchFilter: UserSearchFilter,
        pageable: Pageable,
        sortedBy: String
    ): StaffCaseload {
        val user = userRepository.getUser(username)
        val caseload = caseloadRepository.searchByStaffId(
            user.staff!!.id,
            searchFilter.nameOrCrn,
            searchFilter.nextContactCode,
            searchFilter.sentenceCode,
            pageable
        )
        val sentenceTypes =
            caseloadRepository.findSentenceTypesForStaff(user.staff.id)
                .map { KeyPair(it.code.trim(), it.description) }
        val contactTypes =
            caseloadRepository.findContactTypesForStaff(user.staff.id).map { KeyPair(it.code.trim(), it.description) }
        val userAccess = userAccessService.checkLimitedAccessFor(caseload.content.map { it.person.crn })

        return StaffCaseload(
            totalElements = caseload.totalElements.toInt(),
            totalPages = caseload.totalPages,
            provider = user.staff.provider.description,
            caseload = caseload.content.map { it.toStaffCase(userAccess.access.first { ua -> ua.crn == it.person.crn }) },
            staff = Name(forename = user.staff.forename, surname = user.staff.surname),
            metaData = MetaData(sentenceTypes = sentenceTypes, contactTypes = contactTypes),
            sortedBy = sortedBy
        )
    }

    @Transactional
    fun getTeamCaseload(teamCode: String, pageable: Pageable): TeamCaseload {
        val team = teamRepository.getTeam(teamCode)
        val caseload = caseloadRepository.findByTeamCode(team.code, pageable)

        return TeamCaseload(
            totalElements = caseload.totalElements.toInt(),
            totalPages = caseload.totalPages,
            provider = team.provider.description,
            caseload = caseload.content.map { it.toTeamCase() },
            team = Team(description = team.description, team.code)
        )
    }

    @Transactional
    fun getUserTeams(username: String): UserTeam {
        val user = userRepository.getUser(username)
        val teams = staffRepository.findTeamsByStaffCode(user.staff!!.code)
            .map { Team(description = it.description, code = it.code) }
        return UserTeam(provider = user.staff.provider.description, teams = teams)
    }

    @Transactional
    fun getTeamStaff(teamCode: String): TeamStaff {
        val provider = teamRepository.getProvider(teamCode)
        val staff = teamRepository.findStaffByTeamCode(teamCode)
            .map { Staff(name = Name(forename = it.forename, surname = it.surname), code = it.code) }
        return TeamStaff(provider = provider, staff = staff)
    }
}

fun Caseload.toStaffCase(caseAccess: CaseAccess? = null) = StaffCase(
    limitedAccess = caseAccess.isLao(),
    caseName = Name(
        forename = person.forename,
        middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
        surname = person.surname
    ).takeIf { !caseAccess.isLao() },
    crn = person.crn,
    nextAppointment = nextAppointment?.let {
        Appointment(
            id = it.id,
            description = it.type.description,
            date = it.appointmentDatetime
        )
    }.takeIf { !caseAccess.isLao() },
    previousAppointment = previousAppointment?.let {
        Appointment(
            id = it.id,
            description = it.type.description,
            date = it.appointmentDatetime
        )
    }.takeIf { !caseAccess.isLao() },
    dob = person.dateOfBirth.takeIf { !caseAccess.isLao() },
    latestSentence = latestSentence?.disposal?.type?.description.takeIf { !caseAccess.isLao() },
    numberOfAdditionalSentences = (latestSentence?.let { it.totalNumberOfSentences - 1L }
        ?: 0L).takeIf { !caseAccess.isLao() },
)

fun Caseload.toTeamCase() = TeamCase(
    caseName = Name(
        forename = person.forename,
        middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
        surname = person.surname
    ),
    crn = person.crn,
    staff = Staff(name = Name(forename = staff.forename, surname = staff.surname), code = staff.code)
)

fun CaseAccess?.isLao() = this != null && (this.userExcluded || this.userRestricted)
