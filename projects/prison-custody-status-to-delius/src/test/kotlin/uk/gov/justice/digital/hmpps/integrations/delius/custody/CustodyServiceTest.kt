package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
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
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.IN_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.RELEASED_ON_LICENCE
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

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var prisonManagerService: PrisonManagerService

    @Mock
    private lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    private lateinit var contactAlertRepository: ContactAlertRepository

    @InjectMocks
    private lateinit var contactService: ContactService

    private lateinit var custodyService: CustodyService

    @BeforeEach
    fun setUp() {
        custodyService = CustodyService(
            referenceDataRepository,
            custodyRepository,
            custodyHistoryRepository,
            prisonManagerService,
            contactService
        )
    }

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
        val ct = ContactType(IdGenerator.getAndIncrement(), ContactType.Code.CHANGE_OF_INSTITUTION.value)
        val inCom = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!

        whenever(referenceDataRepository.findByCodeAndSetName(LOCATION_CHANGE.code, "CUSTODY EVENT TYPE"))
            .thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[LOCATION_CHANGE])
        whenever(contactTypeRepository.findByCode(ContactType.Code.CHANGE_OF_INSTITUTION.value)).thenReturn(ct)
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        val recallReason = ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.END_OF_TEMPORARY_LICENCE]
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
        assertThat(contact.firstValue.type.code, equalTo(ContactType.Code.CHANGE_OF_INSTITUTION.value))
        assertThat(contact.firstValue.staffId, equalTo(om.staffId))
        assertThat(contact.firstValue.teamId, equalTo(om.teamId))
        assertThat(
            contact.firstValue.notes,
            equalTo(
                "Custodial Status: ${custody.status.description}\n" +
                    "Custodial Establishment: ${custody.institution?.description}\n" +
                    "Location Change Date: ${DeliusDateTimeFormatter.format(now)}\n" +
                    "-------------------------------" +
                    EOTL_LOCATION_CHANGE_CONTACT_NOTES
            )
        )
    }
}
