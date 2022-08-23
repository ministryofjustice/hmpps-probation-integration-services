package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.institution.InstitutionRepository
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
        val custody = CustodyGenerator.generate(PersonGenerator.RELEASABLE, InstitutionGenerator.RELEASED_FROM)
        val now = ZonedDateTime.now()
        whenever(referenceDataRepository.findByCodeAndSetNameAndSelectableIsTrue(IN_CUSTODY.code, "THROUGHCARE STATUS"))
            .thenReturn(ReferenceDataGenerator.CUSTODIAL_STATUS[IN_CUSTODY])
        whenever(referenceDataRepository.findByCodeAndSetNameAndSelectableIsTrue(STATUS_CHANGE.code, "CUSTODY EVENT TYPE"))
            .thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[STATUS_CHANGE])

        custodyService.updateStatus(custody, IN_CUSTODY, now, "Some detail string")

        verify(custodyRepository).save(custody)
        verify(custodyHistoryRepository).save(
            CustodyHistory(
                date = now,
                type = ReferenceDataGenerator.CUSTODY_EVENT_TYPE[STATUS_CHANGE]!!,
                detail = "Some detail string",
                person = custody.disposal.event.person,
                custody = custody,
            )
        )
    }

    @Test
    fun updateLocationCreatesHistoryRecord() {
        val custody = CustodyGenerator.generate(PersonGenerator.RELEASABLE, InstitutionGenerator.RELEASED_FROM)
        val now = ZonedDateTime.now()
        whenever(institutionRepository.findByCode(InstitutionCode.IN_COMMUNITY.code))
            .thenReturn(InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY])
        whenever(referenceDataRepository.findByCodeAndSetNameAndSelectableIsTrue(LOCATION_CHANGE.code, "CUSTODY EVENT TYPE"))
            .thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[LOCATION_CHANGE])

        custodyService.updateLocation(custody, InstitutionCode.IN_COMMUNITY, now)

        verify(custodyRepository).save(custody)
        verify(custodyHistoryRepository).save(
            CustodyHistory(
                date = now,
                type = ReferenceDataGenerator.CUSTODY_EVENT_TYPE[LOCATION_CHANGE]!!,
                detail = "Test institution",
                person = custody.disposal.event.person,
                custody = custody,
            )
        )
    }
}
