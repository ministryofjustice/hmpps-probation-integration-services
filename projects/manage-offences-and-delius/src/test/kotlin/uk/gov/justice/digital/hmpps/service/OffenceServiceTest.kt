package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COURT_CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.HIGH_LEVEL_OFFENCE
import uk.gov.justice.digital.hmpps.entity.OffenceRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.offenceCode
import uk.gov.justice.digital.hmpps.repository.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class OffenceServiceTest {

    @Mock
    lateinit var detailedOffenceRepository: DetailedOffenceRepository

    @Mock
    lateinit var offenceRepository: OffenceRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @InjectMocks
    lateinit var offenceService: OffenceService

    @Test
    fun `missing reference data is thrown`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offenceCode = notification.message.offenceCode

        assertThrows<NotFoundException> { offenceService.createOffence(offence(offenceCode)) }
            .run { assertThat(message, equalTo("Court category with code of CS not found")) }
    }

    @Test
    fun `offence is created`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offence = offence(notification.message.offenceCode)
        whenever(referenceDataRepository.findByCodeAndSetName(COURT_CATEGORY.code, COURT_CATEGORY.set.name))
            .thenReturn(COURT_CATEGORY)
        whenever(offenceRepository.findByCode(any())).thenReturn(null)
        whenever(offenceRepository.findByCode(HIGH_LEVEL_OFFENCE.code)).thenReturn(HIGH_LEVEL_OFFENCE)

        offenceService.createOffence(offence)

        verify(detailedOffenceRepository).save(check {
            assertThat(it.id, nullValue())
            assertThat(it.code, equalTo(offence.code))
            assertThat(it.category, equalTo(COURT_CATEGORY))
        })
        verify(offenceRepository).save(check {
            assertThat(it.id, nullValue())
            assertThat(it.code, equalTo(offence.homeOfficeCode))
            assertThat(it.description, equalTo("${offence.homeOfficeDescription} - ${offence.homeOfficeCode}"))
            assertThat(it.mainCategoryCode, equalTo(HIGH_LEVEL_OFFENCE.mainCategoryCode))
            assertThat(it.mainCategoryDescription, equalTo(HIGH_LEVEL_OFFENCE.description))
            assertThat(it.subCategoryCode, equalTo(offence.subCategoryCode))
            assertThat(it.subCategoryDescription, equalTo(offence.homeOfficeDescription))
        })
    }

    private fun offence(code: String = "AB12345", homeOfficeCode: String = "091/55") = Offence(
        code = code,
        description = "some offence",
        offenceType = COURT_CATEGORY.code,
        startDate = LocalDate.now(),
        homeOfficeStatsCode = homeOfficeCode,
        homeOfficeDescription = "Some Offence Description",
    )
}