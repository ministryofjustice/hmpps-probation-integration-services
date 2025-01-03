package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ResidenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@Component
class ReferralBookingDataLoader(
    private val personRepository: PersonRepository,
    private val residenceRepository: ResidenceRepository
) {
    fun loadData() {
        personRepository.save(PersonGenerator.PERSON_WITH_BOOKING)
        ReferralGenerator.ARRIVAL = residenceRepository.save(
            ReferralGenerator.generateResidence(
                PersonGenerator.PERSON_WITH_BOOKING,
                ReferralGenerator.BOOKING_ARRIVED_DB_RECORD!!,
                arrivalDateTime = ReferralGenerator.ARRIVAL.arrivalDate,
            )
        )
        ReferralGenerator.DEPARTURE = residenceRepository.save(
            ReferralGenerator.generateResidence(
                PersonGenerator.PERSON_WITH_BOOKING,
                ReferralGenerator.BOOKING_DEPARTED_DB_RECORD!!,
                arrivalDateTime = ReferralGenerator.DEPARTURE.arrivalDate,
                departureDateTime = ReferralGenerator.DEPARTURE.departureDate
            )
        )
    }
}