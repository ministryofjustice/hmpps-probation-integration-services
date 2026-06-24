package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.UserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.utils.Summary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class ScheduleServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var offenderManagerRepository: OffenderManagerRepository

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var enforcementRepository: EnforcementRepository

    @InjectMocks
    lateinit var service: ScheduleService

    @Test
    fun `calls get upcoming schedule function`() {
        val crn = "X000005"
        val expectedContacts = listOf(
            appointmentContact(CreateAppointment.Type.PlannedDoorstepContactNS.code),
            appointmentContact("ZZZZ")
        )
        whenever(personRepository.findSummary(crn)).thenReturn(summaryEntity())
        whenever(contactRepository.findUpComingAppointments(any(), any(), any(), any())).thenReturn(PageImpl(expectedContacts))

        val res = service.getPersonUpcomingSchedule(crn, PageRequest.of(0, 10))

        assertThat(res.personSummary, equalTo(expectedPersonSummary()))
        assertThat(res.personSchedule.appointments, equalTo(expectedContacts.map { it.toActivity() }))
    }

    @Test
    fun `calls get appointment function`() {
        val crn = "X000005"
        val expectedContact = appointmentContact(CreateAppointment.Type.PlannedDoorstepContactNS.code)
        whenever(personRepository.findSummary(crn)).thenReturn(summaryEntity())
        whenever(contactRepository.findByPersonIdAndId(1, expectedContact.id!!)).thenReturn(expectedContact)
        whenever(userRepository.findAllById(any<Iterable<Long>>())).thenReturn(emptyList())

        val res = service.getPersonAppointment(crn, expectedContact.id!!)

        assertThat(res.personSummary, equalTo(expectedPersonSummary()))
        assertThat(res.appointment, equalTo(expectedContact.toActivity()))
    }

    @Test
    fun `calls get previous schedule function`() {
        val crn = "X000005"
        val expectedContacts = listOf(
            appointmentContact("ABCD"),
            appointmentContact("EFGH")
        )
        whenever(personRepository.findSummary(crn)).thenReturn(summaryEntity())
        whenever(contactRepository.findPageablePreviousAppointments(any(), any(), any(), any())).thenReturn(PageImpl(expectedContacts))

        val res = service.getPersonPreviousSchedule(
            crn,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "contact_date", "contact_start_time"))
        )

        assertThat(res.personSummary, equalTo(expectedPersonSummary()))
        assertThat(res.personSchedule.appointments, equalTo(expectedContacts.map { it.toActivity() }))
    }

    @Test
    fun `deliusManaged is false when contact type is in CreateAppointment types and complied is false`() {
        val contact = appointmentContact(
            CreateAppointment.Type.PlannedDoorstepContactNS.code,
            complied = false
        )

        assertThat(contact.toActivity().deliusManaged, equalTo(false))
    }

    @Test
    fun `deliusManaged is true when contact type is unknown`() {
        val contact = appointmentContact("ZZZZ")

        assertThat(contact.toActivity().deliusManaged, equalTo(true))
    }

    @Test
    fun `deliusManaged is true when requirement mainCategory code is F`() {
        val contact = appointmentContact(
            CreateAppointment.Type.PlannedDoorstepContactNS.code,
            requirement = requirement(mainCategoryCode = "F")
        )

        assertThat(contact.toActivity().deliusManaged, equalTo(true))
    }

    private fun summaryEntity() = Summary(
        id = 1,
        forename = "Joe",
        secondName = null,
        thirdName = null,
        surname = "Bloggs",
        crn = "X000005",
        pnc = "1234567",
        noms = "A1234AA",
        dateOfBirth = LocalDateTime.of(1990, 1, 1, 0, 0)
    )

    private fun expectedPersonSummary() = PersonSummary(
        name = Name(forename = "Joe", middleName = null, surname = "Bloggs"),
        crn = "X000005",
        offenderId = 1,
        pnc = "1234567",
        noms = "A1234AA",
        dateOfBirth = LocalDate.of(1990, 1, 1)
    )

    private fun appointmentContact(
        code: String,
        complied: Boolean? = true,
        requirement: Requirement? = null,
    ) = Contact(
        id = 1,
        person = person(),
        type = contactType(code),
        date = LocalDate.of(2024, 6, 24),
        startTime = ZonedDateTime.of(2024, 6, 24, 9, 0, 0, 0, EuropeLondon),
        complied = complied,
        requirement = requirement,
        notes = null,
        lastUpdatedUser = user(),
    )

    private fun person() = Person(
        id = 1,
        crn = "X000005",
        pnc = "1234567",
        noms = "A1234AA",
        forename = "Joe",
        secondName = null,
        thirdName = null,
        surname = "Bloggs",
        preferredName = null,
        dateOfBirth = LocalDate.of(1990, 1, 1),
        dateOfDeath = null,
        telephoneNumber = null,
        mobileNumber = null,
        emailAddress = null,
        previousSurname = null,
        gender = referenceData(1, "M", "Male"),
        religion = null,
        language = null,
        sexualOrientation = referenceData(2, "U", "Unknown"),
        genderIdentity = referenceData(3, "U", "Unknown"),
        genderIdentityDescription = null,
        requiresInterpreter = false,
        softDeleted = false,
        exclusionMessage = null,
        restrictionMessage = null,
        lastUpdatedDatetime = ZonedDateTime.now(EuropeLondon),
        lastUpdatedUserId = 0,
        lastUpdatedUser = null,
    )

    private fun user() = User(
        id = 1,
        staff = null,
        username = "JoeBloggs",
        forename = "Joe",
        surname = "Bloggs",
    )

    private fun contactType(code: String) = ContactType(
        id = 1,
        code = code,
        attendanceContact = true,
        description = "Type $code",
        locationRequired = "N",
        editable = true,
    )

    private fun requirement(mainCategoryCode: String) = Requirement(
        id = 1,
        length = null,
        notes = null,
        expectedStartDate = null,
        startDate = LocalDate.now(),
        commencementDate = null,
        expectedEndDate = null,
        terminationDate = null,
        disposal = null,
        mainCategory = RequirementMainCategory(
            id = 1,
            code = mainCategoryCode,
            description = mainCategoryCode
        ),
        subCategory = null,
        terminationDetails = null,
    )

    private fun referenceData(id: Long, code: String, description: String) = ReferenceData(
        id = id,
        code = code,
        description = description,
    )
}
