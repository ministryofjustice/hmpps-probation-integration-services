package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator.INACTIVE_PERSON_ADDRESS_ID
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator.PERSON_ADDRESS_ID
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator.BOOKING_ARRIVED_DB_RECORD
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator.BOOKING_DEPARTED_DB_RECORD

@Component
class EntityManagerDataLoader {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun loadData() {
        PERSON_ADDRESS_ID = entityManager.merge(AddressGenerator.PERSON_ADDRESS).id
        INACTIVE_PERSON_ADDRESS_ID = entityManager.merge(AddressGenerator.INACTIVE_PERSON_ADDRESS).id
        entityManager.merge(ReferralGenerator.EXISTING_REFERRAL)
        entityManager.merge(ReferralGenerator.BOOKING_WITHOUT_ARRIVAL)
        BOOKING_ARRIVED_DB_RECORD = entityManager.merge(ReferralGenerator.BOOKING_ARRIVED)
        BOOKING_DEPARTED_DB_RECORD = entityManager.merge(ReferralGenerator.BOOKING_DEPARTED)
    }
}