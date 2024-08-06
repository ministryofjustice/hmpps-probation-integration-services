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
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

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
            ReferenceDataGenerator.PRISON,
            InstitutionGenerator.WSIHMP,
            OrganisationGenerator.DEFAULT,
            DocumentEntityGenerator.INSTITUTIONAL_REPORT_TYPE,
            DocumentEntityGenerator.INSTITUTIONAL_REPORT,
            AreaGenerator.PARTITION_AREA,
            ProviderEmployeeGenerator.PROVIDER_EMPLOYEE,
            ProviderGenerator.DEFAULT,
            ProviderTeamGenerator.EXTERNAL_PROVIDER,
            ProviderTeamGenerator.DEFAULT_PROVIDER_TEAM,
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
            ReferenceDataGenerator.MONTHS,
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
            ReferenceDataGenerator.ACR,
            ReferenceDataGenerator.EXP,
            ReferenceDataGenerator.HDE,
            ReferenceDataGenerator.LED,
            ReferenceDataGenerator.PED,
            ReferenceDataGenerator.PSSED,
            ReferenceDataGenerator.POM1,
            ReferenceDataGenerator.POM2,
            ReferenceDataGenerator.SED,
            ReferenceDataGenerator.CRN,
            ReferenceDataGenerator.TRIAL,
            CourtGenerator.PROBATION_AREA,
            CourtGenerator.BHAM,
            PersonGenerator.NEW_TO_PROBATION,
            PersonGenerator.CURRENTLY_MANAGED,
            PersonGenerator.PREVIOUSLY_MANAGED,
            PersonGenerator.NO_SENTENCE,
            PersonGenerator.NO_ACTIVE_EVENTS,
            PersonGenerator.RESTRICTED_CASE,
            PersonGenerator.EXCLUDED_CASE,
            PersonGenerator.PROVISION_1,
            PersonGenerator.DISABILITY_1,
            PersonGenerator.PREVIOUS_CONVICTION_DOC,
            PersonGenerator.ADDRESS,
            PersonGenerator.ALIAS
        )

        em.saveAll(
            StaffGenerator.ALLOCATED,
            StaffGenerator.UNALLOCATED,
            StaffGenerator.OFFICER,
            StaffGenerator.STAFF_USER
        )

        em.saveAll(
            PersonGenerator.generatePersonManager(PersonGenerator.NEW_TO_PROBATION),
            PersonGenerator.generatePersonManager(PersonGenerator.CURRENTLY_MANAGED),
        )

        val noSentenceEvent =
            SentenceGenerator.generateEvent(PersonGenerator.NO_SENTENCE, referralDate = LocalDate.now())
        val noSentenceManager =
            SentenceGenerator.generateOrderManager(
                noSentenceEvent,
                StaffGenerator.UNALLOCATED,
                CourtGenerator.PROBATION_AREA,
                ZonedDateTime.of(LocalDate.now(), LocalTime.NOON, EuropeLondon),
                ZonedDateTime.of(LocalDate.now().minusDays(1), LocalTime.NOON, EuropeLondon)
            )
        val outcome = SentenceGenerator.OUTCOME
        val courtAppearance = SentenceGenerator.generateCourtAppearance(noSentenceEvent, outcome, ZonedDateTime.now())
        em.saveAll(noSentenceEvent, noSentenceManager, outcome, courtAppearance)

        val newEvent = SentenceGenerator.generateEvent(PersonGenerator.NEW_TO_PROBATION, referralDate = LocalDate.now())
        val newSentence =
            SentenceGenerator.generateSentence(newEvent, LocalDate.now(), DisposalTypeGenerator.CURFEW_ORDER)
        val newManager =
            SentenceGenerator.generateOrderManager(
                newEvent,
                StaffGenerator.UNALLOCATED,
                CourtGenerator.PROBATION_AREA,
                ZonedDateTime.of(LocalDate.now().minusDays(1), LocalTime.NOON, EuropeLondon),
                ZonedDateTime.of(LocalDate.now().minusDays(3), LocalTime.NOON, EuropeLondon)
            )
        em.saveAll(newEvent, newSentence, newManager)

        val currentEvent = SentenceGenerator.CURRENTLY_MANAGED
        val currentSentence = SentenceGenerator.CURRENT_SENTENCE
        val custody = SentenceGenerator.CURRENT_CUSTODY
        val currentManager = SentenceGenerator.CURRENT_ORDER_MANAGER
        val mainOffence = SentenceGenerator.MAIN_OFFENCE_DEFAULT
        val additionalOffence = SentenceGenerator.ADDITIONAL_OFFENCE_DEFAULT
        val licenceCondition = SentenceGenerator.generateLicenseCondition(disposal = currentSentence)
        val breachNsi = SentenceGenerator.BREACH_NSIS
        val activePssRequirement = SentenceGenerator.generatePssRequirement(custody.id, active = true)
        val inactivePssRequirement = SentenceGenerator.generatePssRequirement(custody.id, active = false)
        val currentCourtAppearance = SentenceGenerator.COURT_APPEARANCE
        val currentCourtReport = SentenceGenerator.generateCourtReport(currentCourtAppearance)
        val reportManager = SentenceGenerator.generateCourtReportManager(currentCourtReport)

        em.saveAll(
            currentEvent,
            SentenceGenerator.INACTIVE_EVENT,
            SentenceGenerator.INACTIVE_EVENT_1,
            currentSentence,
            RequirementsGenerator.ACTIVE_REQ,
            RequirementsGenerator.INACTIVE_REQ,
            RequirementsGenerator.DELETED_REQ,
            RequirementsGenerator.INACTIVE_AND_DELETED_REQ,
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
            SentenceGenerator.CONDITIONAL_RELEASE_KEY_DATE,
            SentenceGenerator.LED_KEY_DATE,
            SentenceGenerator.HDC_KEY_DATE,
            SentenceGenerator.PAROLE_KEY_DATE,
            SentenceGenerator.SENTENCE_KEY_DATE,
            SentenceGenerator.EXPECTED_RELEASE_KEY_DATE,
            SentenceGenerator.SUPERVISION_KEY_DATE,
            SentenceGenerator.HANDOVER_START_KEY_DATE,
            SentenceGenerator.HANDOVER_KEY_DATE,
            mainOffence,
            SentenceGenerator.MAIN_OFFENCE_FOR_INACTIVE_EVENT,
            SentenceGenerator.MAIN_OFFENCE_FOR_INACTIVE_EVENT_1,
            additionalOffence,
            licenceCondition,
            SentenceGenerator.ACTIVE_NSI_STATUS,
            breachNsi,
            NsiManagerGenerator.ACTIVE,
            NsiManagerGenerator.INACTIVE,
            activePssRequirement,
            inactivePssRequirement,
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
        val preManager =
            SentenceGenerator.generateOrderManager(
                preEvent,
                StaffGenerator.ALLOCATED,
                CourtGenerator.PROBATION_AREA,
                ZonedDateTime.of(LocalDate.now().minusDays(7), LocalTime.NOON, EuropeLondon),
                ZonedDateTime.of(LocalDate.now().minusDays(10), LocalTime.NOON, EuropeLondon)
            )
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

        em.persist(PersonGenerator.PRISON_MANAGER)
        em.persist(PersonGenerator.RESPONSIBLE_OFFICER)

        em.saveAll(
            ContactGenerator.ATTENDANCE_OUTCOME,
            ContactGenerator.ATTENDANCE_CONTACT_TYPE,
            ContactGenerator.ATTENDANCE_CONTACT_1,
            ContactGenerator.ATTENDANCE_CONTACT_2
        )
    }
}

fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
