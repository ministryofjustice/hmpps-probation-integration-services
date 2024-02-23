package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.cvl.ActivatedLicence
import uk.gov.justice.digital.hmpps.integrations.cvl.ApConditions
import uk.gov.justice.digital.hmpps.integrations.cvl.Conditions
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
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
    fun `when multiple active custodial sentence it is logged to telemetry`() {
        val crn = "M728831"
        val person = PersonGenerator.generatePerson(crn)
        val activatedLicence = ActivatedLicence(
            crn,
            LocalDate.now(),
            Conditions(ApConditions(listOf(), listOf(), listOf()))
        )
        val occurredAt = ZonedDateTime.now()
        whenever(personManagerRepository.findByPersonCrn(crn)).thenReturn(PersonGenerator.DEFAULT_CM)
        whenever(custodyRepository.findCustodialSentences(crn)).thenReturn(
            listOf(
                SentenceGenerator.generate(SentenceGenerator.generateEvent("1", person)),
                SentenceGenerator.generate(SentenceGenerator.generateEvent("2", person))
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
                    "Multiple Custodial Sentences",
                    mapOf(
                        "crn" to crn,
                        "startDate" to activatedLicence.startDate.toString(),
                        "occurredAt" to occurredAt.toString(),
                        "sentenceCount" to "2"
                    )
                )
            )
        )
    }
}
