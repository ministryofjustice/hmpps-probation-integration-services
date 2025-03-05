package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.repository.BoroughRepository
import uk.gov.justice.digital.hmpps.data.repository.DistrictRepository
import uk.gov.justice.digital.hmpps.data.repository.StaffWithTeamsRepository
import uk.gov.justice.digital.hmpps.data.repository.TeamWithDistrictRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository

@Component
@ConditionalOnProperty("seed.database")
class ExistingAllocationsDataLoader(
    private val boroughRepository: BoroughRepository,
    private val districtRepository: DistrictRepository,
    private val teamWithDistrictRepository: TeamWithDistrictRepository,
    private val staffRepository: StaffRepository,
    private val staffWithTeamsRepository: StaffWithTeamsRepository,
    private val eventRepository: EventRepository,
) {
    fun loadData() {
        boroughRepository.save(ProviderGenerator.PDU)
        districtRepository.save(ProviderGenerator.LAU)
        teamWithDistrictRepository.save(TeamGenerator.TEAM_IN_LAU)
        staffRepository.save(StaffGenerator.ALLOCATED)
        eventRepository.save(EventGenerator.HAS_INITIAL_ALLOCATION)
        staffWithTeamsRepository.save(StaffGenerator.STAFF_WITH_TEAM)
    }
}

