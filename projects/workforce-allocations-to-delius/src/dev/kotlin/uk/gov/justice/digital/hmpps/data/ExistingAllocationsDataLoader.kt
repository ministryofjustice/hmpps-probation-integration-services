package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator.DEFAULT
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator.generate
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class ExistingAllocationsDataLoader(private val dataManager: DataManager) {
    fun loadData() {
        dataManager.save(ProviderGenerator.PDU)
        dataManager.save(ProviderGenerator.LAU)
        dataManager.save(TeamGenerator.TEAM_IN_LAU)
        dataManager.save(StaffGenerator.ALLOCATED)
        dataManager.save(EventGenerator.HAS_INITIAL_ALLOCATION)
        dataManager.save(StaffGenerator.STAFF_WITH_TEAM)
        dataManager.save(StaffGenerator.END_DATED_STAFF_WITH_TEAM)
        dataManager.save(StaffGenerator.STAFF_WITH_TEAM_AND_USER)
        dataManager.save(StaffGenerator.END_DATED_STAFF_WITH_USER)
        val previous = dataManager.save(
            generate(
                staff = StaffGenerator.ALLOCATED,
                startDateTime = ManagerGenerator.START_DATE_TIME.minusDays(7),
                endDateTime = DEFAULT.startDate
            )
        )
        dataManager.save(
            generate(
                startDateTime = ManagerGenerator.START_DATE_TIME.minusDays(14),
                endDateTime = previous.startDate
            )
        )
    }
}
