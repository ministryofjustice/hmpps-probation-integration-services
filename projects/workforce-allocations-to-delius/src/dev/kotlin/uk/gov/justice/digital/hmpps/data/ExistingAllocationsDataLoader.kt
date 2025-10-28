package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator.DEFAULT
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator.generate
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.repository.BoroughRepository
import uk.gov.justice.digital.hmpps.data.repository.DistrictRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithTeamsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamWithDistrictRepository
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
    private val orderManagerRepository: OrderManagerRepository,
) {
    fun loadData() {
        boroughRepository.save(ProviderGenerator.PDU)
        districtRepository.save(ProviderGenerator.LAU)
        teamWithDistrictRepository.save(TeamGenerator.TEAM_IN_LAU)
        staffRepository.save(StaffGenerator.ALLOCATED)
        eventRepository.save(EventGenerator.HAS_INITIAL_ALLOCATION)
        staffWithTeamsRepository.save(StaffGenerator.STAFF_WITH_TEAM)
        staffWithTeamsRepository.save(StaffGenerator.END_DATED_STAFF_WITH_TEAM)
        staffWithTeamUserRepository.save(StaffGenerator.STAFF_WITH_TEAM_AND_USER)
        staffWithTeamUserRepository.save(StaffGenerator.END_DATED_STAFF_WITH_USER)
        val previous = orderManagerRepository.save(
            generate(
                staff = StaffGenerator.ALLOCATED,
                startDateTime = ManagerGenerator.START_DATE_TIME.minusDays(7),
                endDateTime = DEFAULT.startDate
            )
        )
        orderManagerRepository.save(
            generate(
                startDateTime = ManagerGenerator.START_DATE_TIME.minusDays(14),
                endDateTime = previous.startDate
            )
        )
    }
}

interface StaffWithTeamUserRepository : JpaRepository<StaffWithTeamUser, Long>

