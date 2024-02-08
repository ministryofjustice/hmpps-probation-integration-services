package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.cvl.ActivatedLicence
import uk.gov.justice.digital.hmpps.integrations.cvl.ApConditions
import uk.gov.justice.digital.hmpps.integrations.cvl.Conditions
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMappingRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceDataRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class LicenceConditionApplierTest {
    @Mock
    internal lateinit var disposalRepository: DisposalRepository

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
    fun `when no active custodial sentence an error is thrown`() {
        val crn = "N678461"
        whenever(personManagerRepository.findByPersonCrn(crn)).thenReturn(PersonGenerator.DEFAULT_CM)
        whenever(disposalRepository.findCustodialSentences(crn)).thenReturn(listOf())

        val ex = assertThrows<IllegalStateException> {
            licenceConditionApplier.applyLicenceConditions(
                crn,
                ActivatedLicence(
                    crn,
                    LocalDate.now(),
                    null,
                    null,
                    Conditions(ApConditions(listOf(), listOf(), listOf()))
                ),
                ZonedDateTime.now()
            )
        }
        assertThat(ex.message, equalTo("No Custodial Sentences to apply Licence Conditions"))
    }
}
