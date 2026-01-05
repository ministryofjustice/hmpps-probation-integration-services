package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class ReferralBookingDataLoader(private val dataManager: DataManager) {
    fun loadData() {
        dataManager.save(PersonGenerator.PERSON_WITH_BOOKING)
        dataManager.save(ReferralGenerator.ARRIVAL)
        dataManager.save(ReferralGenerator.DEPARTURE)
    }
}