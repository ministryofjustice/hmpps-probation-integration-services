package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import java.util.*

@Service
class OffenderManagerService(
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate
) {

    fun getAllOffenderManagersForCrn(
        crn: String,
        includeProbationAreaTeams: Boolean
    ): List<CommunityOrPrisonOffenderManager> {
        return getAllOffenderManagers(personRepository.getPerson(crn), includeProbationAreaTeams)
    }

    private fun getAllOffenderManagers(
        offender: Person,
        includeProbationAreaTeams: Boolean
    ): List<CommunityOrPrisonOffenderManager> {
        return offender.offenderManagers
            .filter(PersonManager::active)
            .map { offenderManager: PersonManager -> this.addLdapFields(offenderManager) }
            .map { offMgr -> offMgr.toOffenderManager(includeProbationAreaTeams) } +
            offender.prisonOffenderManagers
                .filter(PrisonManager::active)
                .map { offMgr -> offMgr.toOffenderManager(includeProbationAreaTeams) }
    }

    fun addLdapFields(offenderManager: PersonManager): PersonManager {

        Optional.ofNullable(offenderManager.staff)
            .map<StaffUser>(Staff::user)
            .map<LdapUser> { u -> ldapTemplate.findByUsername<LdapUser>(u.username) }
            .ifPresent { staffDetails: LdapUser ->
                offenderManager.telephoneNumber = staffDetails.telephoneNumber
                offenderManager.emailAddress = staffDetails.email?.takeIf { email -> email.isNotBlank() }
            }
        return offenderManager
    }
}

fun PersonManager.toOffenderManager(includeProbationAreaTeams: Boolean) = CommunityOrPrisonOffenderManager(
    staffCode = staff?.code,
    staffId = staff?.id,
    isUnallocated = isUnallocated(),
    staff = staff?.let {
        ContactableHuman(
            forenames = listOfNotNull(it.forename, staff.forename2).joinToString(" "),
            surname = staff.surname,
            email = emailAddress,
            phoneNumber = telephoneNumber
        )
    },
    team = team?.toTeam(),
    isPrisonOffenderManager = false,
    probationArea = provider.toProbationArea(includeProbationAreaTeams),
    isResponsibleOfficer = responsibleOfficer() != null,
    fromDate = date.toLocalDate(),
    grade = staff?.grade?.keyValueOf()
)

fun PrisonManager.toOffenderManager(includeProbationAreaTeams: Boolean) = CommunityOrPrisonOffenderManager(
    staffCode = staff.code,
    staffId = staff.id,
    isUnallocated = isUnallocated(),
    staff = ContactableHuman(
        forenames = listOfNotNull(staff.forename, staff.forename2).joinToString(" "),
        surname = staff.surname,
        email = emailAddress,
        phoneNumber = telephoneNumber
    ),
    team = team.toTeam(),
    isPrisonOffenderManager = true,
    probationArea = probationArea.toProbationArea(includeProbationAreaTeams),
    isResponsibleOfficer = responsibleOfficer() != null,
    fromDate = date.toLocalDate()
)

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team.toTeam() = Team(
    code = code.trim(),
    description = description,
    telephone = telephone,
    emailAddress = emailAddress,
    borough = KeyValue(district.borough.code, district.borough.description),
    district = KeyValue(district.code, district.description),
    localDeliveryUnit = KeyValue(district.code, district.description),
    teamType = KeyValue(ldu.code, ldu.description),
    startDate = startDate,
    endDate = endDate
)

fun uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Institution.toInstitution() = Institution(
    institutionId = id.institutionId,
    code = code,
    description = description,
    establishmentType = establishmentType?.keyValueOf(),
    institutionName = institutionName,
    isEstablishment = id.establishment,
    isPrivate = private,
    nomsPrisonInstitutionCode = nomisCdeCode
)

fun ProbationAreaEntity.toProbationArea(includeProbationAreaTeams: Boolean) = ProbationArea(
    code = code,
    description = description,
    organisation = KeyValue(organisation.code, organisation.description),
    probationAreaId = id,
    institution = institution?.toInstitution(),
    teams = if (includeProbationAreaTeams) {
        teams.map {
            AllTeam(
                code = it.code,
                description = it.description,
                district = KeyValue(it.district.code, it.district.description),
                borough = KeyValue(it.district.borough.code, it.district.borough.description),
                localDeliveryUnit = KeyValue(it.ldu.code, it.ldu.description),
                isPrivate = it.private,
                teamId = it.id,
                scProvider = KeyValue(it.code, it.description)
            )
        } + providerTeams.map {
            AllTeam(
                providerTeamId = it.providerTeamId,
                code = it.code,
                name = it.name,
                externalProvider = KeyValue(it.externalProvider.code, it.externalProvider.description)
            )
        }
    } else null,
)
