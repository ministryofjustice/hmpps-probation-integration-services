package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.repository.BoroughRepository
import uk.gov.justice.digital.hmpps.data.repository.DistrictRepository
import uk.gov.justice.digital.hmpps.data.repository.StaffWithTeamsRepository
import uk.gov.justice.digital.hmpps.data.repository.TeamWithDistrictRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffWithTeamUser

@Component
@ConditionalOnProperty("seed.database")
class ExistingAllocationsDataLoader(
    private val boroughRepository: BoroughRepository,
    private val districtRepository: DistrictRepository,
    private val teamWithDistrictRepository: TeamWithDistrictRepository,
    private val staffRepository: StaffRepository,
    private val staffWithTeamsRepository: StaffWithTeamsRepository,
    private val eventRepository: EventRepository,
    private val staffWithTeamUserRepository: StaffWithTeamUserRepository,
) {
    fun loadData() {
        boroughRepository.save(ProviderGenerator.PDU)
        districtRepository.save(ProviderGenerator.LAU)
        teamWithDistrictRepository.save(TeamGenerator.TEAM_IN_LAU)
        staffRepository.save(StaffGenerator.ALLOCATED)
        eventRepository.save(EventGenerator.HAS_INITIAL_ALLOCATION)
        val staffWithTeam = staffWithTeamsRepository.save(StaffGenerator.STAFF_WITH_TEAM)
        staffWithTeamUserRepository.save(StaffWithTeamUser("s001wt", staffWithTeam, IdGenerator.getAndIncrement()))
    }
}

interface StaffWithTeamUserRepository : JpaRepository<StaffWithTeamUser, Long>

