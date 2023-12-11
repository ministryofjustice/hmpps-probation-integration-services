package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.DEFAULT_CUSTODY
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateCustodialSentence
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateEvent
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateOrderManager
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.repository.DatasetRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.EventRepository
import uk.gov.justice.digital.hmpps.data.repository.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val datasetRepository: DatasetRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val disposalRepository: DisposalRepository,
    private val custodyRepository: CustodyRepository,
    private val keyDateRepository: KeyDateRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        datasetRepository.saveAll(
            listOf(
                ReferenceDataGenerator.DS_CUSTODY_STATUS,
                ReferenceDataGenerator.DS_KEY_DATE_TYPE
            )
        )
        referenceDataRepository.save(ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS)
        referenceDataRepository.saveAll(ReferenceDataGenerator.KEY_DATE_TYPES.values)
        contactTypeRepository.save(ContactTypeGenerator.EDSS)

        personRepository.save(PersonGenerator.DEFAULT)

        val event = eventRepository.save(generateEvent(PersonGenerator.DEFAULT))
        orderManagerRepository.save(generateOrderManager(event))
        val disposal = disposalRepository.save(generateDisposal(event))
        DEFAULT_CUSTODY = custodyRepository.save(
            generateCustodialSentence(
                ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS,
                disposal,
                "38339A"
            )
        )

        keyDateRepository.saveAll(
            listOf(
                KeyDateGenerator.generate(
                    DEFAULT_CUSTODY,
                    ReferenceDataGenerator.KEY_DATE_TYPES["PED"]!!,
                    LocalDate.parse("2022-10-26")
                ),
                KeyDateGenerator.generate(
                    DEFAULT_CUSTODY,
                    ReferenceDataGenerator.KEY_DATE_TYPES["LED"]!!,
                    LocalDate.parse("2024-09-10")
                )
            )
        )
        createPersonWithKeyDates(PersonGenerator.PERSON_WITH_KEYDATES, "38340A")
    }

    private fun createPersonWithKeyDates(personRef: Person, bookingRef: String): Custody {
        val person = personRepository.save(personRef)
        val event = eventRepository.save(generateEvent(person, "1"))
        orderManagerRepository.save(generateOrderManager(event))
        val disposal = disposalRepository.save(generateDisposal(event))
        val custody = custodyRepository.save(
            generateCustodialSentence(
                ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS,
                disposal,
                bookingRef
            )
        )
        keyDateRepository.saveAll(
            ReferenceDataGenerator.KEY_DATE_TYPES.values.map {
                KeyDateGenerator.generate(custody, it, LocalDate.parse("2023-12-11"))
            }
        )
        return custody
    }
}
