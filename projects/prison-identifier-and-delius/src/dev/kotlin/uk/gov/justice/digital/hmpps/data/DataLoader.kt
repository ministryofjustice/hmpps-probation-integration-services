package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateCustody
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateEvent
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateOrderManager
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import java.time.LocalDate

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        val personWithNomsEvent = generateEvent(PersonGenerator.PERSON_WITH_NOMS)
        val personWithNomsDisposal = generateDisposal(LocalDate.of(2022, 11, 11), personWithNomsEvent)
        val personWithNomsCustody = generateCustody(personWithNomsDisposal)
        val personWithNomsOrderManager = generateOrderManager(personWithNomsEvent)

        val personWithNoNomsNumberEvent = generateEvent(PersonGenerator.PERSON_WITH_NO_NOMS)
        val personWithNoNomsNumberDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithNoNomsNumberEvent)
        val personWithNoNomsNumberCustody = generateCustody(personWithNoNomsNumberDisposal)
        val personWithNoNomsNumberOrderManager = generateOrderManager(personWithNoNomsNumberEvent)

        val personWithMultiMatchEvent = generateEvent(PersonGenerator.PERSON_WITH_MULTI_MATCH)
        val personWithMultiMatchDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithMultiMatchEvent)
        val personWithMultiMatchCustody = generateCustody(personWithMultiMatchDisposal)
        val personWithMultiMatchOrderManager = generateOrderManager(personWithMultiMatchEvent)

        val personWithNoMatchEvent = generateEvent(PersonGenerator.PERSON_WITH_NO_MATCH)
        val personWithNoMatchDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithNoMatchEvent)
        val personWithNoMatchCustody = generateCustody(personWithNoMatchDisposal)
        val personWithNoMatchOrderManager = generateOrderManager(personWithNoMatchEvent)

        val personWithNomsInDeliusEvent = generateEvent(PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS)
        val personWithNomsInDeliusDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithNomsInDeliusEvent)
        val personWithNomsInDeliusCustody = generateCustody(personWithNomsInDeliusDisposal)
        val personWithNomsInDeliusOrderManager = generateOrderManager(personWithNomsInDeliusEvent)

        val personWithNomsInDeliusEventDb = generateEvent(PersonGenerator.PERSON_WITH_NOMS_DB)
        val personWithNomsInDeliusDisposalDb =
            generateDisposal(LocalDate.of(2022, 11, 11), personWithNomsInDeliusEventDb)
        val personWithNomsInDeliusCustodyDb = generateCustody(personWithNomsInDeliusDisposalDb)
        val personWithNomsInDeliusOrderManagerDb = generateOrderManager(personWithNomsInDeliusEventDb)

        saveAll(
            ReferenceDataGenerator.GENDER_SET,
            ReferenceDataGenerator.MALE,
            ReferenceDataGenerator.CUSTODY_STATUS_SET,
            ReferenceDataGenerator.CUSTODY_STATUS,
            ReferenceDataGenerator.ADDITIONAL_IDENTIFIER_TYPE_SET,
            ReferenceDataGenerator.DUPLICATE_NOMS,
            ReferenceDataGenerator.FORMER_NOMS,
            ReferenceDataGenerator.CONTACT_TYPE,
            PersonGenerator.PERSON_WITH_NOMS,
            PersonGenerator.PERSON_WITH_NO_NOMS,
            PersonGenerator.PERSON_WITH_MULTI_MATCH,
            PersonGenerator.PERSON_WITH_NO_MATCH,
            PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS,
            PersonGenerator.PERSON_WITH_DUPLICATE_NOMS,
            PersonGenerator.PERSON_WITH_EXISTING_NOMS,
            PersonGenerator.PERSON_WITH_NOMS_DB,
            PersonGenerator.PERSON_ALIAS_1,
            PersonGenerator.PERSON_ALIAS_2,
            PersonGenerator.OFFENDER_MANAGER,
            personWithNomsEvent,
            personWithNomsDisposal,
            personWithNomsCustody,
            personWithNomsOrderManager,
            personWithNoNomsNumberEvent,
            personWithNoNomsNumberDisposal,
            personWithNoNomsNumberCustody,
            personWithNoNomsNumberOrderManager,
            personWithMultiMatchEvent,
            personWithMultiMatchDisposal,
            personWithMultiMatchCustody,
            personWithMultiMatchOrderManager,
            personWithNoMatchEvent,
            personWithNoMatchDisposal,
            personWithNoMatchCustody,
            personWithNoMatchOrderManager,
            personWithNomsInDeliusEvent,
            personWithNomsInDeliusDisposal,
            personWithNomsInDeliusCustody,
            personWithNomsInDeliusOrderManager,
            personWithNomsInDeliusEventDb,
            personWithNomsInDeliusDisposalDb,
            personWithNomsInDeliusCustodyDb,
            personWithNomsInDeliusOrderManagerDb,
        )
    }
}
