package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.LC_STANDARD_CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.LC_STANDARD_SUB_CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.cvl.ActivatedLicence
import uk.gov.justice.digital.hmpps.integrations.cvl.ApConditions
import uk.gov.justice.digital.hmpps.integrations.cvl.Conditions
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory.Companion.STANDARD_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceData.Companion.STANDARD_SUB_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class LicenceConditionApplierTest {
    @Mock
    internal lateinit var custodyRepository: CustodyRepository

    @Mock
    internal lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    internal lateinit var cvlMappingRepository: CvlMappingRepository

    @Mock
    internal lateinit var licenceConditionCategoryRepository: LicenceConditionCategoryRepository

    @Mock
    internal lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    internal lateinit var licenceConditionService: LicenceConditionService

    @Mock
    internal lateinit var contactService: ContactService

    @Mock
    internal lateinit var optimisationTables: OptimisationTables

    @InjectMocks
    internal lateinit var licenceConditionApplier: LicenceConditionApplier

    @Test
    fun `when no active custodial sentence it is logged to telemetry`() {
        val crn = "N678461"
        val activatedLicence = ActivatedLicence(
            crn,
            LocalDate.now(),
            Conditions(ApConditions(listOf(), listOf(), listOf()))
        )
        val occurredAt = ZonedDateTime.now()
        whenever(personManagerRepository.findByPersonCrn(crn)).thenReturn(PersonGenerator.DEFAULT_CM)
        whenever(custodyRepository.findCustodialSentences(crn)).thenReturn(listOf())

        val ex = licenceConditionApplier.applyLicenceConditions(
            crn,
            activatedLicence,
            occurredAt
        )
        assertThat(
            ex.first(), equalTo(
                ActionResult.Ignored(
                    "No Custodial Sentences",
                    mapOf(
                        "crn" to crn,
                        "startDate" to activatedLicence.startDate.toString(),
                        "occurredAt" to occurredAt.toString(),
                        "sentenceCount" to "0"
                    )
                )
            )
        )
    }

    @Test
    fun `when SED date is in the past, no custodial sentences are found and logged to telemetry`() {
        val crn = "K918361"
        val person = PersonGenerator.generatePerson(crn)
        val activatedLicence = ActivatedLicence(
            crn,
            LocalDate.now(),
            Conditions(ApConditions(listOf(), listOf(), listOf()))
        )
        val occurredAt = ZonedDateTime.now()
        val sentence = SentenceGenerator.generate(SentenceGenerator.generateEvent("1", person), endDate = LocalDate.now())
        val keyDates = listOf(
            SentenceGenerator.generateKeyDate(
                sentence,
                ReferenceDataGenerator.SENTENCE_EXPIRY_DATE_TYPE,
                LocalDate.now().minusDays(1)
            )
        )
        sentence.set("keyDates", keyDates)

        whenever(personManagerRepository.findByPersonCrn(crn)).thenReturn(PersonGenerator.DEFAULT_CM)
        whenever(custodyRepository.findCustodialSentences(crn)).thenReturn(listOf(sentence))

        val ex = licenceConditionApplier.applyLicenceConditions(
            crn,
            activatedLicence,
            occurredAt
        )
        assertThat(
            ex.first(), equalTo(
                ActionResult.Ignored(
                    "No Custodial Sentences",
                    mapOf(
                        "crn" to crn,
                        "startDate" to activatedLicence.startDate.toString(),
                        "occurredAt" to occurredAt.toString(),
                        "sentenceCount" to "0"
                    )
                )
            )
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["endDate", "enteredEndDate"])
    fun `when multiple active custodial sentence create CVL where end date is populated`(field: String ) {
        val crn = "M728831"
        val person = PersonGenerator.generatePerson(crn)
        val activatedLicence = ActivatedLicence(
            crn,
            LocalDate.now(),
            Conditions(ApConditions(listOf(), listOf(), listOf()))
        )
        val occurredAt = ZonedDateTime.now()
        whenever(personManagerRepository.findByPersonCrn(crn)).thenReturn(PersonGenerator.DEFAULT_CM)

        var sentence2:Custody? = null
        if (field == "endDate") {
            sentence2 = SentenceGenerator.generate(SentenceGenerator.generateEvent("2", person), endDate = LocalDate.now())
        }

        if (field == "enteredEndDate") {
            sentence2 = SentenceGenerator.generate(
                SentenceGenerator.generateEvent("2", person),
                endDate = LocalDate.now().plusDays(7),
                enteredEndDate = LocalDate.now())
        }

        whenever(custodyRepository.findCustodialSentences(crn)).thenReturn(
            listOf(
                SentenceGenerator.generate(SentenceGenerator.generateEvent("1", person), endDate = LocalDate.now().minusDays(1)),
                sentence2!!,
                SentenceGenerator.generate(SentenceGenerator.generateEvent("3", person), endDate = LocalDate.now().minusDays(7)),
            )
        )

        whenever(licenceConditionCategoryRepository.findByCode(STANDARD_CATEGORY_CODE)).thenReturn(
            LC_STANDARD_CATEGORY
        )
        whenever(referenceDataRepository.findByCodeAndDatasetCode(STANDARD_SUB_CATEGORY_CODE, Dataset.SUB_CATEGORY_CODE)).thenReturn(
            LC_STANDARD_SUB_CATEGORY
        )
        val results = licenceConditionApplier.applyLicenceConditions(
            crn,
            activatedLicence,
            occurredAt
        )

        assertThat(
            results.first(), equalTo(
                ActionResult.Success(
                    ActionResult.Type.StandardLicenceConditionAdded,
                    mapOf(
                        "crn" to crn,
                        "eventNumber" to "2",
                        "startDate" to activatedLicence.startDate.toString(),
                        "standardConditions" to "0",
                        "additionalConditions" to "0",
                        "bespokeConditions" to "0"
                    )
                )
            )
        )

    }

    @Test
    fun `no start date is logged to telemetry`() {
        val crn = "S728831"
        val person = PersonGenerator.generatePerson(crn)
        val activatedLicence = ActivatedLicence(
            crn,
            null,
            Conditions(ApConditions(listOf(), listOf(), listOf()))
        )
        val occurredAt = ZonedDateTime.now()
        whenever(personManagerRepository.findByPersonCrn(crn)).thenReturn(PersonGenerator.DEFAULT_CM)
        whenever(custodyRepository.findCustodialSentences(crn)).thenReturn(
            listOf(
                SentenceGenerator.generate(SentenceGenerator.generateEvent("1", person), endDate = LocalDate.now())
            )
        )

        val ex = licenceConditionApplier.applyLicenceConditions(
            crn,
            activatedLicence,
            occurredAt
        )
        assertThat(
            ex.first(), equalTo(
                ActionResult.Ignored(
                    "No Start Date",
                    mapOf(
                        "crn" to crn,
                        "eventNumber" to "1",
                        "startDate" to "null",
                        "standardConditions" to "0",
                        "additionalConditions" to "0",
                        "bespokeConditions" to "0"
                    )
                )
            )
        )
    }
}
