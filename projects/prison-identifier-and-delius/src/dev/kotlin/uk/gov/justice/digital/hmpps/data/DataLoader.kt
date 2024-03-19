package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateCustody
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateEvent
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val em: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        val personWithNomsEvent = generateEvent(PersonGenerator.PERSON_WITH_NOMS)
        val personWithNomsDisposal = generateDisposal(LocalDate.of(2022, 11, 11), personWithNomsEvent)
        val personWithNomsCustody = generateCustody(personWithNomsDisposal)

        val personWithNoNomsNumberEvent = generateEvent(PersonGenerator.PERSON_WITH_NO_NOMS)
        val personWithNoNomsNumberDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithNoNomsNumberEvent)
        val personWithNoNomsNumberCustody = generateCustody(personWithNoNomsNumberDisposal)

        val personWithMultiMatchEvent = generateEvent(PersonGenerator.PERSON_WITH_MULTI_MATCH)
        val personWithMultiMatchDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithMultiMatchEvent)
        val personWithMultiMatchCustody = generateCustody(personWithMultiMatchDisposal)

        val personWithNoMatchEvent = generateEvent(PersonGenerator.PERSON_WITH_NO_MATCH)
        val personWithNoMatchDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithNoMatchEvent)
        val personWithNoMatchCustody = generateCustody(personWithNoMatchDisposal)

        val personWithNomsInDeliusEvent = generateEvent(PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS)
        val personWithNomsInDeliusDisposal = generateDisposal(LocalDate.of(2022, 12, 12), personWithNomsInDeliusEvent)
        val personWithNomsInDeliusCustody = generateCustody(personWithNomsInDeliusDisposal)

        em.saveAll(
            ReferenceDataGenerator.GENDER_SET,
            ReferenceDataGenerator.MALE,
            ReferenceDataGenerator.CUSTODY_STATUS_SET,
            ReferenceDataGenerator.CUSTODY_STATUS,
            ReferenceDataGenerator.ADDITIONAL_IDENTIFIER_TYPE_SET,
            ReferenceDataGenerator.DUPLICATE_NOMS,
            ReferenceDataGenerator.FORMER_NOMS,
            PersonGenerator.PERSON_WITH_NOMS,
            PersonGenerator.PERSON_WITH_NO_NOMS,
            PersonGenerator.PERSON_WITH_MULTI_MATCH,
            PersonGenerator.PERSON_WITH_NO_MATCH,
            PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS,
            PersonGenerator.PERSON_WITH_DUPLICATE_NOMS,
            PersonGenerator.PERSON_WITH_EXISTING_NOMS,
            personWithNomsEvent,
            personWithNomsDisposal,
            personWithNomsCustody,
            personWithNoNomsNumberEvent,
            personWithNoNomsNumberDisposal,
            personWithNoNomsNumberCustody,
            personWithMultiMatchEvent,
            personWithMultiMatchDisposal,
            personWithMultiMatchCustody,
            personWithNoMatchEvent,
            personWithNoMatchDisposal,
            personWithNoMatchCustody,
            personWithNomsInDeliusEvent,
            personWithNomsInDeliusDisposal,
            personWithNomsInDeliusCustody

        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
