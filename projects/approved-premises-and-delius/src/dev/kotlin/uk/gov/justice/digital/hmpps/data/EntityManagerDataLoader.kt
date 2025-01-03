package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator

@Component
class EntityManagerDataLoader {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun loadData() {
        AddressGenerator.PERSON_ADDRESS_ID = entityManager.merge(AddressGenerator.PERSON_ADDRESS).id
        AddressGenerator.INACTIVE_PERSON_ADDRESS_ID = entityManager.merge(AddressGenerator.INACTIVE_PERSON_ADDRESS).id
        entityManager.merge(ReferralGenerator.EXISTING_REFERRAL)
        entityManager.merge(ReferralGenerator.BOOKING_WITHOUT_ARRIVAL)
        ReferralGenerator.BOOKING_ARRIVED_DB_RECORD = entityManager.merge(ReferralGenerator.BOOKING_ARRIVED)
        ReferralGenerator.BOOKING_DEPARTED_DB_RECORD = entityManager.merge(ReferralGenerator.BOOKING_DEPARTED)
    }
}