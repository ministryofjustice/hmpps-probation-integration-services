package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ResidenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@Component
class ReferralBookingDataLoader(
    private val personRepository: PersonRepository,
    private val referralRepository: ReferralRepository,
    private val residenceRepository: ResidenceRepository
) {
    fun loadData() {
        personRepository.save(PersonGenerator.PERSON_WITH_BOOKING)
        referralRepository.save(ReferralGenerator.BOOKING_WITHOUT_ARRIVAL)
        ReferralGenerator.BOOKING_ARRIVED = referralRepository.save(ReferralGenerator.BOOKING_ARRIVED)
        ReferralGenerator.BOOKING_DEPARTED = referralRepository.save(ReferralGenerator.BOOKING_DEPARTED)
        ReferralGenerator.ARRIVAL = residenceRepository.save(
            ReferralGenerator.generateResidence(
                PersonGenerator.PERSON_WITH_BOOKING,
                ReferralGenerator.BOOKING_ARRIVED,
                arrivalDateTime = ReferralGenerator.ARRIVAL.arrivalDate,
            )
        )
        ReferralGenerator.DEPARTURE = residenceRepository.save(
            ReferralGenerator.generateResidence(
                PersonGenerator.PERSON_WITH_BOOKING,
                ReferralGenerator.BOOKING_DEPARTED,
                arrivalDateTime = ReferralGenerator.DEPARTURE.arrivalDate,
                departureDateTime = ReferralGenerator.DEPARTURE.departureDate
            )
        )
    }
}