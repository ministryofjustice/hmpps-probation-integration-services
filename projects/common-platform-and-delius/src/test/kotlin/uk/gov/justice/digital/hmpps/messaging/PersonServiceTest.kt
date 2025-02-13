package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generatorimport.CourtCentreGenerator
import uk.gov.justice.digital.hmpps.data.generatorimport.DefendantGenerator
import uk.gov.justice.digital.hmpps.integrations.client.OsClient
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class PersonServiceTest {

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var courtRepository: CourtRepository

    @Mock
    lateinit var equalityRepository: EqualityRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var personAddressRepository: PersonAddressRepository

    @Mock
    lateinit var osClient: OsClient

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var personService: PersonService

    @Test
    fun `insert person saves multiple records`() {
        val person = PersonGenerator.DEFAULT

        mockReferenceData()

        whenever(personRepository.save(any())).thenReturn(person)
        whenever(courtRepository.findByOuCode(any())).thenReturn(CourtGenerator.UNKNOWN_COURT_N07_PROVIDER)
        whenever(personRepository.getNextCrn()).thenReturn(person.crn)
        whenever(personRepository.getSoundex(any())).thenReturn(person.firstNameSoundex)
        whenever(teamRepository.findByCode(any())).thenReturn(TeamGenerator.UNALLOCATED)
        whenever(staffRepository.findByCode(any())).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(personManagerRepository.save(any())).thenReturn(PersonManagerGenerator.DEFAULT)
        whenever(equalityRepository.save(any())).thenReturn(EqualityGenerator.DEFAULT)
        whenever(osClient.searchByFreeText(any(), any(), any())).thenReturn(OsPlacesResponseGenerator.SINGLE_RESULT)
        whenever(personAddressRepository.save(any())).thenReturn(PersonAddressGenerator.MAIN_ADDRESS)

        val savedPerson = personService.insertPerson(
            defendant = DefendantGenerator.DEFAULT,
            courtCode = CourtCentreGenerator.DEFAULT.code
        )

        verify(personRepository).save(any())
        verify(personManagerRepository).save(any())
        verify(equalityRepository).save(any())
        verify(personAddressRepository).save(check {
            assertThat(it.notes, containsString("UPRN:"))
        })

        assertThat(savedPerson.person.id, equalTo(person.id))
        assertThat(savedPerson.personManager.person.id, equalTo(person.id))
        assertThat(savedPerson.equality.personId, equalTo(person.id))
        assertThat(savedPerson.address?.person?.id, equalTo(person.id))
    }

    @Test
    fun `insert person with no address lookup result uses fallback address`() {
        val person = PersonGenerator.DEFAULT

        mockReferenceData()

        whenever(personRepository.save(any())).thenReturn(person)
        whenever(courtRepository.findByOuCode(any())).thenReturn(CourtGenerator.UNKNOWN_COURT_N07_PROVIDER)
        whenever(personRepository.getNextCrn()).thenReturn(person.crn)
        whenever(personRepository.getSoundex(any())).thenReturn(person.firstNameSoundex)
        whenever(teamRepository.findByCode(any())).thenReturn(TeamGenerator.UNALLOCATED)
        whenever(staffRepository.findByCode(any())).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(personManagerRepository.save(any())).thenReturn(PersonManagerGenerator.DEFAULT)
        whenever(equalityRepository.save(any())).thenReturn(EqualityGenerator.DEFAULT)
        whenever(osClient.searchByFreeText(any(), any(), any())).thenReturn(OsPlacesResponseGenerator.NO_RESULTS)
        whenever(personAddressRepository.save(any())).thenReturn(PersonAddressGenerator.MAIN_ADDRESS)

        val savedPerson = personService.insertPerson(
            defendant = DefendantGenerator.DEFAULT,
            courtCode = CourtCentreGenerator.DEFAULT.code
        )

        verify(personRepository).save(any())
        verify(personManagerRepository).save(any())
        verify(equalityRepository).save(any())
        verify(personAddressRepository).save(check {
            assertThat(
                it.notes,
                Matchers.equalTo("This address record was initially created using information from HMCTS Common Platform.")
            )
        })

        assertThat(savedPerson.person.id, equalTo(person.id))
        assertThat(savedPerson.personManager.person.id, equalTo(person.id))
        assertThat(savedPerson.equality.personId, equalTo(person.id))
        assertThat(savedPerson.address?.person?.id, equalTo(person.id))
    }

    private fun mockReferenceData() {
        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.GenderCode.MALE.deliusValue,
                DatasetCode.GENDER
            )
        )
            .thenReturn(ReferenceDataGenerator.GENDER_MALE)

        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
                DatasetCode.OM_ALLOCATION_REASON
            )
        )
            .thenReturn(ReferenceDataGenerator.INITIAL_ALLOCATION)

        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code,
                DatasetCode.ADDRESS_STATUS
            )
        )
            .thenReturn(ReferenceDataGenerator.MAIN_ADDRESS_STATUS)

        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code,
                DatasetCode.ADDRESS_TYPE
            )
        )
            .thenReturn(ReferenceDataGenerator.AWAITING_ASSESSMENT)
    }
}