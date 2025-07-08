package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.ldap.query.SearchScope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.appointment.UserAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.UserAppointments
import uk.gov.justice.digital.hmpps.api.model.appointment.UserDiary
import uk.gov.justice.digital.hmpps.api.model.overview.Appointment
import uk.gov.justice.digital.hmpps.api.model.user.*
import uk.gov.justice.digital.hmpps.api.model.user.Provider
import uk.gov.justice.digital.hmpps.api.model.user.Staff
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.aspect.DeliusUserAspect
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffUserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.*
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Appointment as AppointmentEntity

@Service
class UserService(
    private val userRepository: UserRepository,
    private val caseloadRepository: CaseloadRepository,
    private val staffRepository: StaffRepository,
    private val staffUserRepository: StaffUserRepository,
    private val teamRepository: TeamRepository,
    private val userAccessService: UserAccessService,
    private val contactRepository: ContactRepository,
    private val probationAreaUserRepository: ProbationAreaUserRepository,
    private val ldapTemplate: LdapTemplate,
    private val deliusUserAspect: DeliusUserAspect
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

    fun getUserStaffId(): Long? {
        return deliusUserAspect.getDeliusUsername()?.let { username ->
            userRepository.findByUsername(username)?.staff?.id
        }
    }

    fun getUpcomingAppointments(username: String, pageable: Pageable): UserDiary {
        val user = getUser(username)

        return user.staff?.let {
            val contacts = contactRepository.findUpComingAppointmentsByUser(
                user.staff.id,
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                ZonedDateTime.now(EuropeLondon).format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon)),
                pageable
            )

            return populateUserDiary(pageable, contacts)
        } ?: UserDiary(pageable.pageSize, pageable.pageNumber, 0, 0, listOf())
    }

    fun getAppointmentsWithoutOutcomes(username: String, pageable: Pageable): UserDiary {
        val user = getUser(username)

        // In mpop api getUserAppointments is called to populate mpop homepage.
        // When api getUserAppointmentsWithoutOutcomes is then called when selecting a page link,
        // this with quick.  However, if the user updates the browser with /caseload/appointments/no-outcome,
        // this will result in api getUserAppointmentsWithoutOutcomes, which is slow.
        // getUserAppointments was previously updated to improve sql performance, and adding the code below will load
        // data in sql cache to speed up contactRepository.findAppointmentsWithoutOutcomesByUser
        getSummaryOfAppointmentsWithoutOutcomes(
            username,
            PageRequest.of(0, 5).withSort(Sort.by(Sort.Direction.ASC, "c.contact_date", "c.contact_start_time"))
        )

        return user.staff?.let {
            val contacts = contactRepository.findAppointmentsWithoutOutcomesByUser(
                user.staff.id,
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                ZonedDateTime.now(EuropeLondon).format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon)),
                pageable
            )

            populateUserDiary(pageable, contacts)
        } ?: UserDiary(pageable.pageSize, pageable.pageNumber, 0, 0, listOf())
    }

    fun getSummaryOfAppointmentsWithoutOutcomes(username: String, pageable: Pageable): UserDiary {
        val user = getUser(username)

        return user.staff?.let {
            val contacts = contactRepository.findSummaryOfAppointmentsWithoutOutcomesByUser(
                user.staff.id,
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                ZonedDateTime.now(EuropeLondon).format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon)),
                pageable
            )

            populateUserDiary(pageable, contacts)
        } ?: UserDiary(pageable.pageSize, pageable.pageNumber, 0, 0, listOf())
    }

    fun getAppointmentsForUser(username: String): UserAppointments {
        val user = getUser(username)

        val pageRequest = PageRequest.of(0, 5)
        return user.staff?.let {
            val appointmentsForToday = getUpcomingAppointments(
                username,
                pageRequest.withSort(Sort.by(Sort.Direction.ASC, "contact_date", "contact_start_time"))
            )
            val appointmentsWithoutOutcomes = getSummaryOfAppointmentsWithoutOutcomes(
                username,
                pageRequest.withSort(Sort.by(Sort.Direction.ASC, "c.contact_date", "c.contact_start_time"))
            )

            UserAppointments(
                Name(user.forename, surname = user.surname),
                totalAppointments = appointmentsForToday.totalResults,
                appointments = appointmentsForToday.appointments,
                totalOutcomes = appointmentsWithoutOutcomes.totalResults,
                outcomes = appointmentsWithoutOutcomes.appointments
            )
        } ?: UserAppointments(Name(user.forename, surname = user.surname), totalAppointments = 0, totalOutcomes = 0)
    }

    fun getProvidersForUser(username: String, region: String? = null, team: String? = null): UserProviderResponse {
        val homeArea = ldapTemplate.findAttributeByUsername(username, "userHomeArea")
            ?: throw NotFoundException("No home area found for $username")

        val providers = probationAreaUserRepository.findByUsername(username)
            .map { it.toProvider() }

        val regionSearch = region ?: homeArea

        val defaultTeam = if (region == null && team == null) {
            getDefaultTeam(username, homeArea)
        } else null

        val teams = teamRepository.findByProviderCode(regionSearch).map { it.toTeam() }

        val teamSearch = team ?: defaultTeam?.code ?: teams.first().code
        val users = staffUserRepository.findStaffByTeam(teamSearch).map { it.toUser() }

        return UserProviderResponse(
            getDefaultUserDetails(username, homeArea, providers, defaultTeam),
            providers,
            teams,
            users
        )
    }

    fun getDefaultUserDetails(
        username: String,
        homeArea: String,
        providers: List<Provider>,
        defaultTeam: Team?
    ): DefaultUserDetails {
        val team = defaultTeam ?: getDefaultTeam(username, homeArea)

        return DefaultUserDetails(username, providers.first { it.code == homeArea }.name, team?.description)
    }

    fun getDefaultTeam(username: String, homeArea: String): Team? {
        val defaultTeamId = ldapTemplate.findPreferenceByUsername(username, "defaultTeam")?.toLongOrNull()
        return defaultTeamId?.let { teamRepository.getByTeamById(it) }?.toTeam() ?: teamRepository.getByUserAndProvider(
            username,
            homeArea
        )?.get(0)?.toTeam()
    }

    fun getUser(username: String) =
        userRepository.findUserByUsername(username) ?: throw NotFoundException("User", "username", username)

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

fun populateUserDiary(
    pageable: Pageable,
    contacts: Page<AppointmentEntity>
) = UserDiary(
    pageable.pageSize,
    pageable.pageNumber,
    contacts.totalElements.toInt(),
    contacts.totalPages,
    contacts.content.map {
        it.toUserAppointment()
    }
)

private fun AppointmentEntity.toUserAppointment() = UserAppointment(
    Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname),
    id,
    crn,
    dob,
    sentenceDescription,
    totalSentences?.let { if (it > 0) it - 1 else it },
    contactDescription,
    ZonedDateTime.of(LocalDateTime.of(contactDate, contactStartTime), EuropeLondon),
    if (contactEndTime != null) ZonedDateTime.of(
        LocalDateTime.of(contactDate, contactEndTime),
        EuropeLondon
    ) else null,
    location
)

fun ProbationAreaUser.toProvider() = Provider(id.provider.code, id.provider.description)