package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.DocumentType
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.Outcome
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
        em.saveAll(
            DocumentEntityGenerator.COURT,
            DocumentEntityGenerator.INSTITUTIONAL_REPORT_TYPE,
            DocumentEntityGenerator.INSTITUTIONAL_REPORT,
            DocumentEntityGenerator.R_INSTITUTION,
            AreaGenerator.PARTITION_AREA,
            ProviderEmployeeGenerator.PROVIDER_EMPLOYEE,
            ProviderGenerator.DEFAULT,
            LDUGenerator.DEFAULT,
            BoroughGenerator.DEFAULT,
            DistrictGenerator.DEFAULT,
            TeamGenerator.DEFAULT,
            ReferenceDataGenerator.VIOLENCE,
            SentenceGenerator.MAIN_OFFENCE,
            SentenceGenerator.ADDITIONAL_OFFENCE,
            BusinessInteractionGenerator.UPDATE_CONTACT,
            ContactTypeGenerator.CONTACT_TYPE,

            DisposalTypeGenerator.CURFEW_ORDER,
            ReferenceDataGenerator.DISPOSAL_TYPE,
            ReferenceDataGenerator.LENGTH_UNITS,
            ReferenceDataGenerator.TERMINATION_REASON,
            ReferenceDataGenerator.CUSTODIAL_STATUS,
            ReferenceDataGenerator.REQUIREMENT_MAIN_CAT,
            ReferenceDataGenerator.REQUIREMENT_SUB_CAT,
            ReferenceDataGenerator.AD_REQUIREMENT_MAIN_CAT,
            ReferenceDataGenerator.AD_REQUIREMENT_SUB_CAT,
            ReferenceDataGenerator.LIC_COND_MAIN_CAT,
            ReferenceDataGenerator.LIC_COND_SUB_CAT,
            ReferenceDataGenerator.NSI_TYPE,
            ReferenceDataGenerator.NSI_BREACH_OUTCOME,
            ReferenceDataGenerator.PSS_MAIN_CAT,
            ReferenceDataGenerator.PSS_SUB_CAT,
            ReferenceDataGenerator.COURT_REPORT_TYPE,
            ReferenceDataGenerator.GENDER_MALE,
            ReferenceDataGenerator.PROVISION_TYPE_1,
            ReferenceDataGenerator.PROVISION_CATEGORY_1,
            ReferenceDataGenerator.DISABILITY_TYPE_1,
            ReferenceDataGenerator.DISABILITY_CONDITION_1,
            ReferenceDataGenerator.GENDER_IDENTITY,
            ReferenceDataGenerator.ETHNICITY,
            ReferenceDataGenerator.IMMIGRATION_STATUS,
            ReferenceDataGenerator.NATIONALITY,
            ReferenceDataGenerator.LANGUAGE_ENG,
            ReferenceDataGenerator.RELIGION,
            ReferenceDataGenerator.SECOND_NATIONALITY,
            ReferenceDataGenerator.SEXUAL_ORIENTATION,
            ReferenceDataGenerator.TITLE,
            ReferenceDataGenerator.DEFAULT_ADDRESS_TYPE,
            ReferenceDataGenerator.DEFAULT_ADDRESS_STATUS,
            ReferenceDataGenerator.DEFAULT_ALLOCATION_REASON,
            ReferenceDataGenerator.DEFAULT_TIER,
            ReferenceDataGenerator.REF_DISQ,
            PersonGenerator.NEW_TO_PROBATION,
            PersonGenerator.CURRENTLY_MANAGED,
            PersonGenerator.PREVIOUSLY_MANAGED,
            PersonGenerator.NO_SENTENCE,
            PersonGenerator.PROVISION_1,
            PersonGenerator.DISABILITY_1,
            PersonGenerator.PREVIOUS_CONVICTION_DOC,
            PersonGenerator.ADDRESS,
            PersonGenerator.ALIAS
        )

        em.saveAll(StaffGenerator.ALLOCATED, StaffGenerator.UNALLOCATED, StaffGenerator.OFFICER)

        em.saveAll(
            PersonGenerator.generatePersonManager(PersonGenerator.NEW_TO_PROBATION),
            PersonGenerator.generatePersonManager(PersonGenerator.CURRENTLY_MANAGED)
        )

        val noSentenceEvent =
            SentenceGenerator.generateEvent(PersonGenerator.NO_SENTENCE, referralDate = LocalDate.now())
        val noSentenceManager = SentenceGenerator.generateOrderManager(noSentenceEvent, StaffGenerator.UNALLOCATED)
        val outcome = Outcome(Outcome.Code.AWAITING_PSR.value, IdGenerator.getAndIncrement())
        val courtAppearance = SentenceGenerator.generateCourtAppearance(noSentenceEvent, outcome)
        em.saveAll(noSentenceEvent, noSentenceManager, outcome, courtAppearance)

        val newEvent = SentenceGenerator.generateEvent(PersonGenerator.NEW_TO_PROBATION, referralDate = LocalDate.now())
        val newSentence =
            SentenceGenerator.generateSentence(newEvent, LocalDate.now(), DisposalTypeGenerator.CURFEW_ORDER)
        val newManager = SentenceGenerator.generateOrderManager(newEvent, StaffGenerator.UNALLOCATED)
        em.saveAll(newEvent, newSentence, newManager)

        val currentEvent = SentenceGenerator.CURRENTLY_MANAGED
        val currentSentence = SentenceGenerator.CURRENT_SENTENCE
        val custody = SentenceGenerator.generateCustody(currentSentence, ReferenceDataGenerator.CUSTODIAL_STATUS)
        val currentManager = SentenceGenerator.generateOrderManager(currentEvent, StaffGenerator.ALLOCATED)
        val mainOffence = SentenceGenerator.MAIN_OFFENCE_DEFAULT
        val additionalOffence = SentenceGenerator.ADDITIONAL_OFFENCE_DEFAULT
        val requirement = SentenceGenerator.generateRequirement(disposal = currentSentence)
        val licenceCondition = SentenceGenerator.generateLicenseCondition(disposal = currentSentence)
        val breachNsi = SentenceGenerator.generateBreachNsi(disposal = currentSentence)
        val pssRequirement = SentenceGenerator.generatePssRequirement(custody.id)
        val currentCourtAppearance = SentenceGenerator.generateCourtAppearance(currentEvent, outcome)
        val currentCourtReport = SentenceGenerator.generateCourtReport(currentCourtAppearance)
        val reportManager = SentenceGenerator.generateCourtReportManager(currentCourtReport)

        em.saveAll(
            currentEvent,
            currentSentence,
            AdditionalSentenceGenerator.SENTENCE_DISQ,
            ReferenceDataGenerator.HOURS_WORKED,
            UnpaidWorkGenerator.UNPAID_WORK_DETAILS_1,
            UnpaidWorkGenerator.APPT1,
            UnpaidWorkGenerator.APPT2,
            UnpaidWorkGenerator.APPT3,
            UnpaidWorkGenerator.APPT4,
            UnpaidWorkGenerator.APPT5,
            UnpaidWorkGenerator.APPT6,
            UnpaidWorkGenerator.APPT7,
            currentManager,
            custody,
            mainOffence,
            additionalOffence,
            requirement,
            licenceCondition,
            breachNsi,
            pssRequirement,
            currentCourtAppearance,
            currentCourtReport,
            reportManager
        )

        val preEvent =
            SentenceGenerator.generateEvent(
                PersonGenerator.PREVIOUSLY_MANAGED,
                referralDate = LocalDate.now(),
                active = false
            )
        val preSentence = SentenceGenerator.generateSentence(
            preEvent,
            LocalDate.now(),
            DisposalTypeGenerator.CURFEW_ORDER,
            terminationDate = LocalDate.now().minusDays(7),
            active = false
        )
        val preManager = SentenceGenerator.generateOrderManager(preEvent, StaffGenerator.ALLOCATED)
        em.saveAll(preEvent, preSentence, preManager)

        em.merge(CourtCaseNoteGenerator.CASE_NOTE)

        em.saveAll(
            DocumentEntityGenerator.generateDocument(
                PersonGenerator.CURRENTLY_MANAGED.id,
                currentEvent.id,
                DocumentType.CONVICTION_DOCUMENT.name,
                "EVENT"
            )
        )
    }
}

fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
