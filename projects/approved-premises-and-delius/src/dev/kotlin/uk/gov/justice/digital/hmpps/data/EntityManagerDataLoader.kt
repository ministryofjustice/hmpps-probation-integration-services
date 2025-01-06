package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Referral

@Component
class EntityManagerDataLoader {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    var personAddressId: Long? = null

    var inactivePersonAddressId: Long? = null

    var bookingArrivedDbRecord: Referral? = null

    var bookingDepartedDbRecord: Referral? = null

    @Transactional
    fun loadData() {
        personAddressId = entityManager.merge(AddressGenerator.PERSON_ADDRESS).id
        inactivePersonAddressId = entityManager.merge(AddressGenerator.INACTIVE_PERSON_ADDRESS).id
        entityManager.merge(ReferralGenerator.EXISTING_REFERRAL)
        entityManager.merge(ReferralGenerator.BOOKING_WITHOUT_ARRIVAL)
        bookingArrivedDbRecord = entityManager.merge(ReferralGenerator.BOOKING_ARRIVED)
        bookingDepartedDbRecord = entityManager.merge(ReferralGenerator.BOOKING_DEPARTED)
    }
}