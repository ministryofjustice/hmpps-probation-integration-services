package uk.gov.justice.digital.hmpps.integrations.delius.user.team

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team

interface TeamRepository : JpaRepository<Team, Long> {
    @Query(
        """
        select t.staff
        from Team t
        where t.code = :teamCode
    """
    )
    fun findStaffByTeamCode(teamCode: String): List<Staff>

    @Query(
        """
        select t.provider.description
        from Team t
        where t.code = :teamCode
    """
    )
    fun findProviderByTeamCode(teamCode: String): String?

    @Query(
        """
        select t
        from Team t
        where t.code = :teamCode
    """
    )
    fun findByTeamCode(teamCode: String): Team?

    @Query(
        """
            select t
            from Team t
            where t.provider.code = :code
            and t.startDate <= current_date
            and (t.endDate is null or t.endDate > current_date)
            order by UPPER(t.description) 
        """
    )
    fun findByProviderCode(code: String): List<Team>

    fun findTeamById(id: Long): Team?

    @Query(
        """
            select t
            from Team t
            join ContactStaffTeam cst on cst.id.team.id = t.id
            join contact_staff cs on cs.id = cst.id.staffId
            join cs.user u
            where t.provider.code = :providerCode
            and t.startDate <= current_date
            and (t.endDate is null or t.endDate > current_date)
            and UPPER(u.username) = UPPER(:username)
            order by UPPER(t.description)
        """
    )
    fun findTeamsByUsernameAndProviderCode(username: String, providerCode: String): List<Team>
}

fun TeamRepository.getTeam(teamCode: String) =
    findByTeamCode(teamCode) ?: throw NotFoundException("Team", "teamCode", teamCode)

fun TeamRepository.getProvider(teamCode: String) =
    findProviderByTeamCode(teamCode) ?: throw NotFoundException("Team", "teamCode", teamCode)

fun TeamRepository.getByTeamById(id: Long) =
    findTeamById(id) ?: throw NotFoundException("Team", "id", id)

fun TeamRepository.getByUserAndProvider(
    username: String,
    providerCode: String
): List<Team>? {
    val teams = findTeamsByUsernameAndProviderCode(username, providerCode)
    if (teams.isEmpty()) {
        return null
    }
    return teams
}
