package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.MAPPA_CAT_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.MAPPA_LVL_2
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.enum.RiskLevel
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType.*
import uk.gov.justice.digital.hmpps.enum.RiskType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.OASYS_ASSESSMENT_LOCKED_INCOMPLETE
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(ReferenceDataGenerator.BUSINESS_INTERACTIONS)
        saveAll(ReferenceDataGenerator.COURTS)
        saveAll(ReferenceDataGenerator.OFFENCES)
        saveAll(
            ReferenceDataGenerator.OASYS_ASSESSMENT_STATUS_DATASET,
            *ReferenceDataGenerator.OASYS_ASSESSMENT_STATUSES.toTypedArray(),
        )
        saveAll(
            ReferenceDataGenerator.FLAG_DATASET,
            ReferenceDataGenerator.ROSH_FLAG,
            ReferenceDataGenerator.SAFEGUARDING_FLAG
        )
        saveAll(ReferenceDataGenerator.LEVELS_DATASET, *ReferenceDataGenerator.LEVELS.toTypedArray())
        saveAll(ContactGenerator.TYPES.values)
        saveAll(RegistrationGenerator.TYPES.values)
        saveAll(RegistrationGenerator.ALT_TYPE)
        saveAll(RegistrationGenerator.DUPLICATE_GROUP)
        saveAll(ReferenceDataGenerator.REQ_MAIN_CATS)
        saveAll(
            ReferenceDataGenerator.DOMAIN_EVENT_TYPE_DATASET,
            *ReferenceDataGenerator.DOMAIN_EVENT_TYPES.toTypedArray()
        )
        saveAll(ReferenceDataGenerator.DISPOSAL_TYPE)
        saveAll(
            ReferenceDataGenerator.CATEGORY_DATASET,
            ReferenceDataGenerator.MAPPA_CAT_1,
            ReferenceDataGenerator.MAPPA_LVL_2
        )

        PersonGenerator.NO_ROSH.withEvent().withRiskOfSeriousHarm(M, L)
        PersonGenerator.LOW_ROSH
            .withEvent()
            .withRiskOfSeriousHarm(M, H)
            .withAssessment(
                "2",
                OASYS_ASSESSMENT_LOCKED_INCOMPLETE,
                externalReference = "urn:uk:gov:hmpps:oasys:assessment:2"
            )
            .withAccreditedProgramRequirement()
            .withVisorAndMappa()
        PersonGenerator.MEDIUM_ROSH.withEvent().withRiskOfSeriousHarm(M)
        PersonGenerator.HIGH_ROSH.withEvent().withRiskOfSeriousHarm(L, H).withVisorAndMappa()
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
        PersonGenerator.LOCKED_INCOMPLETE.withEvent()
        PersonGenerator.OGRS4_TEST.withEvent()
        PersonGenerator.OGRS4_TEST_OGRS4_VALUES.withEvent()
    }

    private fun Person.withEvent(softDeleted: Boolean = false, custodial: Boolean = false): Person {
        val personManager = PersonGenerator.generateManager(this)
        val event = PersonGenerator.generateEvent(this, softDeleted = softDeleted)
        if (custodial) event.set(Event::disposal, PersonGenerator.generateDisposal(event))
        this.set(Person::manager, personManager)
        saveAll(this, personManager, event, event.disposal)
        return this
    }

    private fun Person.withAssessment(
        oasysId: String,
        type: ContactType.Code,
        externalReference: String? = null
    ): Person {
        val contact = save(
            ContactGenerator.generateContact(
                this,
                ContactGenerator.TYPES[type.value]!!,
                externalReference = externalReference
            )
        )
        val assessment = AssessmentGenerator.generate(this, contact, LocalDate.parse("2013-06-07"), oasysId = oasysId)
        save(assessment)
        return this
    }

    private fun Person.withRiskOfSeriousHarm(vararg riskOfSeriousHarmTypes: RiskOfSeriousHarmType): Person {
        riskOfSeriousHarmTypes.forEach {
            val type = RegistrationGenerator.TYPES[it.code]
            val contact = save(ContactGenerator.generateContact(this, type!!.registrationContactType!!))
            val reviewContact = save(ContactGenerator.generateContact(this, type.reviewContactType!!))
            val registration =
                RegistrationGenerator.generate(this.id, LocalDate.parse("2023-06-14"), contact, reviewContact, type)
            highestRiskColour = type.colour
            saveAll(this, registration)
        }
        return this
    }

    private fun Person.withRisks(vararg risks: Pair<RegisterType, RiskLevel?>): Person {
        risks.forEach { risk ->
            val type = risk.first
            val level = ReferenceDataGenerator.LEVELS.singleOrNull { it.code == risk.second?.code }
            val registration = RegistrationGenerator.generate(
                personId = this.id,
                date = LocalDate.parse("2023-06-14"),
                contact = save(ContactGenerator.generateContact(this, type.registrationContactType!!)),
                reviewContact = save(
                    ContactGenerator.generateContact(this, type.reviewContactType!!).withNotes("existing notes")
                ),
                type = type,
                level = level
            )
            saveAll(this, registration)
        }
        return this
    }

    private fun Person.withAccreditedProgramRequirement(): Person {
        saveAll(PersonGenerator.generateRequirement(this))
        return this
    }

    private fun Person.withVisorAndMappa(): Person = apply {
        val mappaType = RegistrationGenerator.TYPES[RegisterType.Code.MAPPA.value]!!
        val visorType = RegistrationGenerator.TYPES[RegisterType.Code.VISOR.value]!!
        val contactType = ContactGenerator.TYPES[ContactType.Code.REGISTRATION.value]!!
        val c1 = save(ContactGenerator.generateContact(this, contactType))
        val c2 = save(ContactGenerator.generateContact(this, contactType))
        val mappa =
            RegistrationGenerator.generate(
                this.id,
                LocalDate.parse("2025-04-01"),
                c1,
                null,
                mappaType,
                category = MAPPA_CAT_1,
                level = MAPPA_LVL_2
            )
        val visor =
            RegistrationGenerator.generate(this.id, LocalDate.parse("2025-04-01"), c2, null, visorType)
        saveAll(mappa, visor)
    }
}
