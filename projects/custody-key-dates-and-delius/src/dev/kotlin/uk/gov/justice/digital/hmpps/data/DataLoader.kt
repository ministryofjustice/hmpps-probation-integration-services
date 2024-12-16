package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.DEFAULT_CUSTODY
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateCustodialSentence
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateEvent
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateOrderManager
import uk.gov.justice.digital.hmpps.data.repository.*
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
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
    private val disposalTypeRepository: DisposalTypeRepository,
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
        val keyDateTypes = referenceDataRepository.saveAll(ReferenceDataGenerator.KEY_DATE_TYPES.values)
        contactTypeRepository.save(ContactTypeGenerator.EDSS)
        disposalTypeRepository.save(SentenceGenerator.DEFAULT_DISPOSAL_TYPE)

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
                    keyDateTypes[2], //PED
                    LocalDate.parse("2022-10-26"),
                    false
                ),
                KeyDateGenerator.generate(
                    DEFAULT_CUSTODY,
                    keyDateTypes[0], //LED
                    LocalDate.parse("2024-09-10"),
                    false
                ),
                KeyDateGenerator.generate(
                    DEFAULT_CUSTODY,
                    keyDateTypes[3], //["SED"]!!,
                    LocalDate.parse("2024-08-10"),
                    false
                )
            )
        )
        createPersonWithKeyDates(PersonGenerator.DEFAULT, "58340A", keyDateTypes)

        createPersonWithKeyDates(PersonGenerator.PERSON_WITH_KEYDATES, "38340A", keyDateTypes)

        createPersonWithKeyDates(PersonGenerator.PERSON_WITH_KEYDATES_BY_CRN, "48340A", keyDateTypes)
    }

    private fun createPersonWithKeyDates(
        personRef: Person,
        bookingRef: String,
        keyDateTypes: List<ReferenceData>
    ): Custody {
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
            keyDateTypes.map { referenceData ->
                if (referenceData.code == "LED") {
                    KeyDate(custody, referenceData, LocalDate.parse("2025-09-11")).also { it.softDeleted = true }
                } else {
                    KeyDate(custody, referenceData, LocalDate.parse("2025-12-11")).also { it.softDeleted = false }
                }
            }
        )
        return custody
    }
}
