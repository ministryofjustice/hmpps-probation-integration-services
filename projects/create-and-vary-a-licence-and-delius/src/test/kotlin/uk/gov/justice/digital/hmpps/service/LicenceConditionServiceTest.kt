package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class LicenceConditionServiceTest {

    @Mock
    internal lateinit var licenceConditionRepository: LicenceConditionRepository

    @Mock
    internal lateinit var licenceConditionManagerRepository: LicenceConditionManagerRepository

    @Mock
    internal lateinit var transferReasonRepository: TransferReasonRepository

    @Mock
    internal lateinit var referenceDataRepository: ReferenceDataRepository

    @InjectMocks
    internal lateinit var licenceConditionService: LicenceConditionService

    lateinit var event: Event
    lateinit var disposal: Disposal
    lateinit var cat: LicenceConditionCategory
    lateinit var subCat: ReferenceData
    lateinit var lc: LicenceCondition

    @BeforeEach
    fun setup() {

        event = Event(
            "1",
            PersonGenerator.DEFAULT_PERSON,
            null,
            active = true,
            softDeleted = false,
            id = IdGenerator.getAndIncrement()
        )
        disposal = Disposal(event, true, false, LocalDate.now(), LocalDate.now(), IdGenerator.getAndIncrement())
        cat = LicenceConditionCategory("CODE", IdGenerator.getAndIncrement())
        subCat = ReferenceData("SUBCAT", "SUBCAT", 1L, IdGenerator.getAndIncrement())
        lc = LicenceCondition(
            personId = PersonGenerator.DEFAULT_PERSON.id,
            disposalId = disposal.id,
            startDate = LocalDate.now(),
            mainCategory = cat,
            subCategory = subCat,
            notes = "",
            cvlText = null,
            pendingTransfer = false,
            active = true,
            softDeleted = false,
            version = 0L,
            id = IdGenerator.getAndIncrement()
        )

        whenever(licenceConditionRepository.save(any())).thenReturn(lc)
        whenever(licenceConditionManagerRepository.save(any())).thenReturn(null)
        whenever(transferReasonRepository.findByCode(any())).thenReturn(TransferReason("TEST", 1L))
        whenever(referenceDataRepository.findByCodeAndDatasetCode(any(), any())).thenReturn(
            ReferenceData(
                "TEST",
                "TEST",
                1L,
                1L
            )
        )
    }

    @Test
    fun `cvlText is truncated to 4000 chars when greater than 4000`() {
        val cvlText = 'A'.repeat(4212)
        licenceConditionService.createLicenceCondition(
            disposal,
            LocalDate.now(),
            cat,
            subCat,
            "P",
            cvlText,
            PersonGenerator.DEFAULT_CM
        )
        val lcCaptor = ArgumentCaptor.forClass(LicenceCondition::class.java)
        Mockito.verify(licenceConditionRepository, Mockito.times(1)).save(lcCaptor.capture())
        assertThat(lcCaptor.value.cvlText?.length, equalTo(4000))
    }

    @Test
    fun `cvlText is unchanged when less than 4000 chars`() {
        val cvlText = "AAAAAAAAAA"
        licenceConditionService.createLicenceCondition(
            disposal,
            LocalDate.now(),
            cat,
            subCat,
            "P",
            cvlText,
            PersonGenerator.DEFAULT_CM
        )
        val lcCaptor = ArgumentCaptor.forClass(LicenceCondition::class.java)
        Mockito.verify(licenceConditionRepository, Mockito.times(1)).save(lcCaptor.capture())
        assertThat(lcCaptor.value.cvlText?.length, equalTo(10))
    }

    @Test
    fun `cvlText is unchanged when null`() {
        val cvlText = null
        licenceConditionService.createLicenceCondition(
            disposal,
            LocalDate.now(),
            cat,
            subCat,
            "P",
            cvlText,
            PersonGenerator.DEFAULT_CM
        )
        val lcCaptor = ArgumentCaptor.forClass(LicenceCondition::class.java)
        Mockito.verify(licenceConditionRepository, Mockito.times(1)).save(lcCaptor.capture())
        assertThat(lcCaptor.value.cvlText, equalTo(null))
    }
}

fun Char.repeat(count: Int): String = this.toString().repeat(count)
