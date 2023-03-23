package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.findMainAddress

@ExtendWith(MockitoExtension::class)
internal class CaseSummaryServiceTest {
    @Mock
    lateinit var personRepository: CaseSummaryPersonRepository

    @Mock
    lateinit var addressRepository: CaseSummaryAddressRepository

    @InjectMocks
    lateinit var caseSummaryService: CaseSummaryService

    @Test
    fun `get personal details`() {
        whenever(personRepository.findByCrn(PersonGenerator.CASE_SUMMARY.crn)).thenReturn(PersonGenerator.CASE_SUMMARY)
        whenever(addressRepository.findMainAddress(PersonGenerator.CASE_SUMMARY.id)).thenReturn(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS)

        val personalDetails = caseSummaryService.getPersonalDetails(PersonGenerator.CASE_SUMMARY.crn)

        assertThat(personalDetails.name.forename, equalTo(PersonGenerator.CASE_SUMMARY.forename))
        assertThat(personalDetails.mainAddress!!.streetName, equalTo(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS.streetName))
    }
}
