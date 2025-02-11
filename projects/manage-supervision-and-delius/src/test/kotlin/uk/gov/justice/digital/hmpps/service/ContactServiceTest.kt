package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.Contact
import uk.gov.justice.digital.hmpps.api.model.sentence.ProfessionalContact
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.OFFENDER_MANAGER_INACTIVE
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.PrisonManagerRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ContactServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var offenderManagerRepository: OffenderManagerRepository

    @Mock
    lateinit var prisonManagerRepository: PrisonManagerRepository

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    lateinit var service: ContactService

    val name = Name(
        PersonGenerator.OVERVIEW.forename,
        PersonGenerator.OVERVIEW.secondName,
        PersonGenerator.OVERVIEW.surname
    )

    @Test
    fun `person does not exist`() {
        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenThrow(
            NotFoundException(
                "Person",
                "crn",
                PersonGenerator.OVERVIEW.crn
            )
        )

        val exception = assertThrows<NotFoundException> {
            service.getContacts(PersonGenerator.OVERVIEW.crn)
        }

        assertEquals("Person with crn of X000004 not found", exception.message)

        verify(personRepository, times(1)).findByCrn(PersonGenerator.OVERVIEW.crn)

        verifyNoMoreInteractions(personRepository)
        verifyNoInteractions(offenderManagerRepository)
        verifyNoInteractions(ldapTemplate)
    }

    @Test
    fun `no offender manager records`() {
        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(
            offenderManagerRepository.findOffenderManagersByPersonId(PersonGenerator.OVERVIEW.id)
        ).thenReturn(listOf())

        val exception = assertThrows<NotFoundException> {
            service.getContacts(PersonGenerator.OVERVIEW.crn)
        }

        assertEquals(
            "Offender Manager records with crn of ${PersonGenerator.OVERVIEW.crn} not found",
            exception.message
        )

        verify(personRepository, times(1)).findByCrn(PersonGenerator.OVERVIEW.crn)
        verify(offenderManagerRepository, times(1)).findOffenderManagersByPersonId(
            PersonGenerator.OVERVIEW.id
        )

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoInteractions(ldapTemplate)
    }

    @Test
    fun `return offender manager records`() {

        val contact1 = Contact(
            "Peter Parker",
            null,
            null,
            "Description of N01",
            "Leicestershire All",
            "OMU B",
            allocationDate = LocalDate.of(2025, 2, 10),
            allocatedUntil = null,
            lastUpdated = LocalDate.of(2025, 2, 10),
            responsibleOfficer = false,
            prisonOffenderManager = false
        )
        val contact2 =
            Contact(
                "Bruce Wayne",
                null,
                null,
                "Description of N01",
                "Leicestershire All",
                "OMU B",
                LocalDate.of(2025, 2, 9),
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 9),
                responsibleOfficer = false,
                prisonOffenderManager = false
            )

        val expected =
            ProfessionalContact(name, currentContacts = listOf(contact1), previousContacts = listOf(contact2))

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(
            offenderManagerRepository.findOffenderManagersByPersonId(PersonGenerator.OVERVIEW.id)
        ).thenReturn(
            listOf(OFFENDER_MANAGER_ACTIVE, OFFENDER_MANAGER_INACTIVE)
        )

        val response = service.getContacts(PersonGenerator.OVERVIEW.crn)

        assertEquals(expected, response)

        verify(personRepository, times(1)).findByCrn(PersonGenerator.OVERVIEW.crn)
        verify(offenderManagerRepository, times(1)).findOffenderManagersByPersonId(
            PersonGenerator.OVERVIEW.id
        )

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(offenderManagerRepository)
    }
}

