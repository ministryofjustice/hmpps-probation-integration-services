package uk.gov.justice.digital.hmpps.integrations.tier

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTier
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataSet
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class TierServiceTest {
    @Mock lateinit var tierClient: TierClient
    @Mock lateinit var personRepository: PersonRepository
    @Mock lateinit var referenceDataRepository: ReferenceDataRepository
    @Mock lateinit var managementTierRepository: ManagementTierRepository
    @InjectMocks lateinit var tierService: TierService

    @Test
    fun `should throw exception when reference data not found`() {
        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation("someTierScore", "someCalculationId", ZonedDateTime.now())
        )

        val exception = assertThrows<NotFoundException> {
            tierService.updateTier("someCrn", "someCalculationId")
        }

        assertEquals("TIER with code of someTierScore not found", exception.message)
    }

    @Test
    fun `should throw exception when person not found`() {
        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation("someTierScore", "someCalculationId", ZonedDateTime.now())
        )

        whenever(referenceDataRepository.findByCodeAndSetName("someTierScore", "TIER")).thenReturn(
            ReferenceData(123L, "someCode", ReferenceDataSet(234L, "someName"))
        )

        val exception = assertThrows<NotFoundException> {
            tierService.updateTier("someCrn", "someCalculationId")
        }

        assertEquals("Person with crn of someCrn not found", exception.message)
    }

    @Test
    fun `should throw exception when change reason not found`() {
        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation("someTierScore", "someCalculationId", ZonedDateTime.now())
        )

        whenever(referenceDataRepository.findByCodeAndSetName("someTierScore", "TIER")).thenReturn(
            ReferenceData(123L, "someCode", ReferenceDataSet(234L, "someName"))
        )

        whenever(personRepository.findByCrnAndSoftDeletedIsFalse("someCrn")).thenReturn(
            Person(456L, "someCrn")
        )

        val exception = assertThrows<NotFoundException> {
            tierService.updateTier("someCrn", "someCalculationId")
        }

        assertEquals("TIER CHANGE REASON with code of ATS not found", exception.message)
    }

    @Test
    fun `should save tier update to repository`() {
        val tierCalculationDate = ZonedDateTime.now()

        whenever(tierClient.getTierCalculation(any(), any())).thenReturn(
            TierCalculation("someTierScore", "someCalculationId", tierCalculationDate)
        )

        whenever(referenceDataRepository.findByCodeAndSetName("someTierScore", "TIER")).thenReturn(
            ReferenceData(123L, "someTierScore", ReferenceDataSet(234L, "TIER"))
        )

        whenever(personRepository.findByCrnAndSoftDeletedIsFalse("someCrn")).thenReturn(
            Person(456L, "someCrn")
        )

        whenever(referenceDataRepository.findByCodeAndSetName("ATS", "TIER CHANGE REASON")).thenReturn(
            ReferenceData(789L, "ATS", ReferenceDataSet(234L, "TIER CHANGE REASON"))
        )

        tierService.updateTier("someCrn", "someCalculationId")

        val captor = ArgumentCaptor.forClass(ManagementTier::class.java)
        verify(managementTierRepository).save(captor.capture())
        val saved = captor.value

        assertThat(saved.id.offenderId, equalTo(456L))
        assertThat(saved.id.tierId, equalTo(123L))
        assertThat(saved.id.dateChanged, equalTo(tierCalculationDate))
        assertThat(saved.createdDatetime, isCloseTo(ZonedDateTime.now()))
    }
}
