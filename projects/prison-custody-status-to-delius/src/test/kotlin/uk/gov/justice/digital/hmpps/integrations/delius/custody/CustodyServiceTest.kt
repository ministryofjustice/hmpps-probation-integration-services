package uk.gov.justice.digital.hmpps.integrations.delius.custody

import IdGenerator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.keydate.entity.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.keydate.entity.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.IN_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.RELEASED_ON_LICENCE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.LOCATION_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.STATUS_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import java.time.LocalDate
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

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var keyDateRepository: KeyDateRepository

    @Mock
    private lateinit var prisonManagerService: PrisonManagerService

    @InjectMocks
    private lateinit var custodyService: CustodyService

    @Test
    fun updateStatusCreatesHistoryRecord() {
        val custody = CustodyGenerator.generate(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT)
        val now = ZonedDateTime.now()
        whenever(referenceDataRepository.findByCodeAndSetName(RELEASED_ON_LICENCE.code, "THROUGHCARE STATUS"))
            .thenReturn(ReferenceDataGenerator.CUSTODIAL_STATUS[IN_CUSTODY])
        whenever(referenceDataRepository.findByCodeAndSetName(STATUS_CHANGE.code, "CUSTODY EVENT TYPE"))
            .thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[STATUS_CHANGE])

        custodyService.updateStatus(custody, RELEASED_ON_LICENCE, now, "Some detail string")

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
        val om = OrderManagerGenerator.generate(custody.disposal.event)
        val ct = ContactType(IdGenerator.getAndIncrement(), ContactTypeCode.CHANGE_OF_INSTITUTION.code)
        val inCom = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!

        whenever(referenceDataRepository.findByCodeAndSetName(LOCATION_CHANGE.code, "CUSTODY EVENT TYPE"))
            .thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[LOCATION_CHANGE])
        whenever(contactTypeRepository.findByCode(ContactTypeCode.CHANGE_OF_INSTITUTION.code)).thenReturn(ct)
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        val recallReason = ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.END_OF_TEMPORARY_LICENCE]
        custodyService.updateLocation(custody, inCom, now, om, recallReason)

        val saved = argumentCaptor<CustodyHistory>()
        verify(custodyHistoryRepository).save(saved.capture())
        verify(custodyRepository).save(custody)

        val savedCustody = saved.firstValue
        assertThat(savedCustody.date, equalTo(now))
        assertThat(savedCustody.type, equalTo(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[LOCATION_CHANGE]!!))
        assertThat(savedCustody.detail, equalTo("Test institution (COMMUN)"))
        assertThat(savedCustody.person, equalTo(custody.disposal.event.person))
        assertThat(savedCustody.custody, equalTo(custody))

        val contact = argumentCaptor<Contact>()
        verify(contactRepository).save(contact.capture())
        assertThat(contact.firstValue.date, equalTo(now))
        assertThat(contact.firstValue.type.code, equalTo(ContactTypeCode.CHANGE_OF_INSTITUTION.code))
        assertThat(contact.firstValue.staffId, equalTo(om.staffId))
        assertThat(contact.firstValue.teamId, equalTo(om.teamId))
        assertThat(
            contact.firstValue.notes,
            equalTo(
                "Custodial Status: ${custody.status.description}\n" +
                    "Custodial Establishment: ${custody.institution.description}\n" +
                    "Location Change Date: ${DeliusDateTimeFormatter.format(now)}\n" +
                    "-------------------------------" +
                    EOTL_LOCATION_CHANGE_CONTACT_NOTES
            )
        )
    }

    @Test
    fun `add RoTL end date sets to day before acr`() {
        val acrDate = KeyDate(42, ReferenceDataGenerator.ACR_DATE_TYPE, LocalDate.now().plusDays(7))
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                KeyDate.TypeCode.ROTL_END_DATE.value,
                ReferenceDataSetGenerator.KEY_DATE_TYPE.name
            )
        ).thenReturn(
            ReferenceDataGenerator.generate(
                KeyDate.TypeCode.ROTL_END_DATE.value,
                ReferenceDataSetGenerator.KEY_DATE_TYPE
            )
        )

        val redSaved = argumentCaptor<KeyDate>()
        custodyService.addRotlEndDate(acrDate)

        verify(keyDateRepository).save(redSaved.capture())
        val red = redSaved.firstValue
        assertThat(red.type.code, equalTo(KeyDate.TypeCode.ROTL_END_DATE.value))
        assertThat(red.date, equalTo(LocalDate.now().plusDays(6)))
    }
}
