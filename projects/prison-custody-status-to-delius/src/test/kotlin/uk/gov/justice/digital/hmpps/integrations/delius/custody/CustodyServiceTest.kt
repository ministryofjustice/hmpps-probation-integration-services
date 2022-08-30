package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.IN_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.LOCATION_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.STATUS_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class CustodyServiceTest {

    @Mock
    private lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    private lateinit var custodyRepository: CustodyRepository

    @Mock
    private lateinit var custodyHistoryRepository: CustodyHistoryRepository

    @Mock
    private lateinit var institutionRepository: InstitutionRepository

    @InjectMocks
    private lateinit var custodyService: CustodyService

    @Test
    fun updateStatusCreatesHistoryRecord() {
        val custody = CustodyGenerator.generate(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT)
        val now = ZonedDateTime.now()
        whenever(referenceDataRepository.findByCodeAndSetName(IN_CUSTODY.code, "THROUGHCARE STATUS"))
            .thenReturn(ReferenceDataGenerator.CUSTODIAL_STATUS[IN_CUSTODY])
        whenever(referenceDataRepository.findByCodeAndSetName(STATUS_CHANGE.code, "CUSTODY EVENT TYPE"))
            .thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[STATUS_CHANGE])

        custodyService.updateStatus(custody, IN_CUSTODY, now, "Some detail string")

        val saved = argumentCaptor<CustodyHistory>()
        verify(custodyHistoryRepository).save(saved.capture())
        verify(custodyRepository).save(custody)

        val savedCustody = saved.firstValue
        assertThat(savedCustody.date, equalTo(now))
        assertThat(savedCustody.type, equalTo(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[STATUS_CHANGE]!!))
        assertThat(savedCustody.detail, equalTo("Some detail string"))
        assertThat(savedCustody.person, equalTo(custody.disposal.event.person))
        assertThat(savedCustody.custody, equalTo(custody))
    }

    @Test
    fun updateLocationCreatesHistoryRecord() {
        val custody = CustodyGenerator.generate(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT)
        val now = ZonedDateTime.now()
        whenever(institutionRepository.findByCode(InstitutionCode.IN_COMMUNITY.code))
            .thenReturn(InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY])
        whenever(referenceDataRepository.findByCodeAndSetName(LOCATION_CHANGE.code, "CUSTODY EVENT TYPE"))
            .thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[LOCATION_CHANGE])

        custodyService.updateLocation(custody, InstitutionCode.IN_COMMUNITY.code, now)

        val saved = argumentCaptor<CustodyHistory>()
        verify(custodyHistoryRepository).save(saved.capture())
        verify(custodyRepository).save(custody)

        val savedCustody = saved.firstValue
        assertThat(savedCustody.date, equalTo(now))
        assertThat(savedCustody.type, equalTo(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[LOCATION_CHANGE]!!))
        assertThat(savedCustody.detail, equalTo("Test institution (COMMUN)"))
        assertThat(savedCustody.person, equalTo(custody.disposal.event.person))
        assertThat(savedCustody.custody, equalTo(custody))
    }
}
