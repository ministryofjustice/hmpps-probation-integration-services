package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        val personWithNomsEvent = PersonGenerator.generateEvent(PersonGenerator.PERSON_WITH_NOMS)
        val personWithNoNomsNumberEvent = PersonGenerator.generateEvent(PersonGenerator.PERSON_WITH_NO_NOMS)
        val personWithMultiMatchEvent = PersonGenerator.generateEvent(PersonGenerator.PERSON_WITH_MULTI_MATCH)
        val personWithNoMatchEvent = PersonGenerator.generateEvent(PersonGenerator.PERSON_WITH_NO_MATCH)
        val personWithNomsInDeliusEvent = PersonGenerator.generateEvent(PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS)

        em.saveAll(
            PersonGenerator.MALE,
            PersonGenerator.PERSON_WITH_NOMS,
            PersonGenerator.PERSON_WITH_NO_NOMS,
            PersonGenerator.PERSON_WITH_MULTI_MATCH,
            PersonGenerator.PERSON_WITH_NO_MATCH,
            PersonGenerator.PERSON_WITH_NOMS_IN_DELIUS,
            personWithNomsEvent,
            PersonGenerator.generateDisposal(LocalDate.now(), personWithNomsEvent),
            personWithNoNomsNumberEvent,
            PersonGenerator.generateDisposal(LocalDate.parse("12/12/2022", DateTimeFormatter.ofPattern("MM/dd/yyyy")), personWithNoNomsNumberEvent),
            personWithMultiMatchEvent,
            PersonGenerator.generateDisposal(LocalDate.parse("12/12/2022", DateTimeFormatter.ofPattern("MM/dd/yyyy")), personWithMultiMatchEvent),
            personWithNoMatchEvent,
            PersonGenerator.generateDisposal(LocalDate.parse("12/12/2022", DateTimeFormatter.ofPattern("MM/dd/yyyy")), personWithNoMatchEvent),
            personWithNomsInDeliusEvent,
            PersonGenerator.generateDisposal(LocalDate.parse("12/12/2022", DateTimeFormatter.ofPattern("MM/dd/yyyy")), personWithNomsInDeliusEvent)
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
