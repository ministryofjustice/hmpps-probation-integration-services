package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.enum.RiskLevel
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType.*
import uk.gov.justice.digital.hmpps.enum.RiskType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.set
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        saveAll(*ReferenceDataGenerator.BUSINESS_INTERACTIONS.toTypedArray())
        saveAll(*ReferenceDataGenerator.COURTS.toTypedArray())
        saveAll(*ReferenceDataGenerator.OFFENCES.toTypedArray())
        saveAll(
            ReferenceDataGenerator.FLAG_DATASET,
            ReferenceDataGenerator.ROSH_FLAG,
            ReferenceDataGenerator.SAFEGUARDING_FLAG
        )
        saveAll(ReferenceDataGenerator.LEVELS_DATASET, *ReferenceDataGenerator.LEVELS.toTypedArray())
        saveAll(*ContactGenerator.TYPES.values.toTypedArray())
        saveAll(*RegistrationGenerator.TYPES.values.toTypedArray())
        saveAll(RegistrationGenerator.ALT_TYPE)
        saveAll(RegistrationGenerator.DUPLICATE_GROUP)
        saveAll(*ReferenceDataGenerator.REQ_MAIN_CATS.toTypedArray())
        saveAll(
            ReferenceDataGenerator.DOMAIN_EVENT_TYPE_DATASET,
            *ReferenceDataGenerator.DOMAIN_EVENT_TYPES.toTypedArray()
        )
        saveAll(ReferenceDataGenerator.DISPOSAL_TYPE)

        PersonGenerator.NO_ROSH.withEvent().withRiskOfSeriousHarm(M, L)
        PersonGenerator.LOW_ROSH
            .withEvent()
            .withRiskOfSeriousHarm(M, H)
            .withAssessment("2")
            .withAccreditedProgramRequirement()
        PersonGenerator.MEDIUM_ROSH.withEvent().withRiskOfSeriousHarm(M)
        PersonGenerator.HIGH_ROSH.withEvent().withRiskOfSeriousHarm(L, H)
        PersonGenerator.VERY_HIGH_ROSH.withEvent().withRiskOfSeriousHarm(L, M, H)
        saveAll(PersonGenerator.PERSON_NO_EVENT)
        PersonGenerator.PERSON_SOFT_DELETED_EVENT.withEvent(softDeleted = true).withRiskOfSeriousHarm(L, M, H)
        PersonGenerator.PRISON_ASSESSMENT.withEvent(custodial = true)
        PersonGenerator.NO_EXISTING_RISKS.withEvent()
        PersonGenerator.EXISTING_RISKS.withEvent().withRisks(
            RegistrationGenerator.TYPES[RiskType.CHILDREN.code]!! to RiskLevel.H,
            RegistrationGenerator.TYPES[RiskType.STAFF.code]!! to RiskLevel.V,
            RegistrationGenerator.TYPES[RiskType.KNOWN_ADULT.code]!! to RiskLevel.M,
            RegistrationGenerator.TYPES[RiskType.PUBLIC.code]!! to RiskLevel.M,
            RegistrationGenerator.ALT_TYPE to null,
        )
        PersonGenerator.EXISTING_RISKS_WITHOUT_LEVEL.withEvent().withRisks(
            RegistrationGenerator.TYPES[RiskType.CHILDREN.code]!! to null
        )
        PersonGenerator.FEATURE_FLAG.withEvent().withRiskOfSeriousHarm(V)
    }

    private fun Person.withEvent(softDeleted: Boolean = false, custodial: Boolean = false): Person {
        val personManager = PersonGenerator.generateManager(this)
        val event = PersonGenerator.generateEvent(this, softDeleted = softDeleted)
        if (custodial) event.set(Event::disposal, PersonGenerator.generateDisposal(event))
        saveAll(this, personManager, event, event.disposal)
        this.set(Person::manager, personManager)
        return this
    }

    private fun Person.withAssessment(oasysId: String): Person {
        val contact =
            entityManager.merge(
                ContactGenerator.generateContact(
                    this,
                    ContactGenerator.TYPES[ContactType.Code.OASYS_ASSESSMENT.value]!!
                )
            )
        val assessment = AssessmentGenerator.generate(this, contact, LocalDate.parse("2013-06-07"), oasysId = oasysId)
        entityManager.merge(assessment)
        return this
    }

    private fun Person.withRiskOfSeriousHarm(vararg riskOfSeriousHarmTypes: RiskOfSeriousHarmType): Person {
        riskOfSeriousHarmTypes.forEach {
            val type = RegistrationGenerator.TYPES[it.code]
            val contact = entityManager.merge(ContactGenerator.generateContact(this, type!!.registrationContactType!!))
            val registration = RegistrationGenerator.generate(this.id, LocalDate.parse("2023-06-14"), contact, type)
            val reviewContact = entityManager.merge(ContactGenerator.generateContact(this, type.reviewContactType!!))
            highestRiskColour = type.colour
            saveAll(this, registration.withReview(reviewContact))
        }
        return this
    }

    private fun Person.withRisks(vararg risks: Pair<RegisterType, RiskLevel?>): Person {
        risks.forEach { risk ->
            val type = risk.first
            val level = ReferenceDataGenerator.LEVELS.singleOrNull { it.code == risk.second?.code }
            val contact = entityManager.merge(ContactGenerator.generateContact(this, type.registrationContactType!!))
            val registration =
                RegistrationGenerator.generate(this.id, LocalDate.parse("2023-06-14"), contact, type, level)
            val reviewContact = entityManager.merge(ContactGenerator.generateContact(this, type.reviewContactType!!))
            saveAll(this, registration.withReview(reviewContact))
        }
        return this
    }

    private fun Person.withAccreditedProgramRequirement(): Person {
        saveAll(PersonGenerator.generateRequirement(this))
        return this
    }

    private fun saveAll(vararg entities: Any?) = entities.filterNotNull().forEach(entityManager::merge)
}
