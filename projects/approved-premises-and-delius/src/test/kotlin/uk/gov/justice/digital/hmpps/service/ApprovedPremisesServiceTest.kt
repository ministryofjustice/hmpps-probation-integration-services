package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventDetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffMemberGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationAssessed
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationSubmitted
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookingMade
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonNotArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.SubmittedBy
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url
import uk.gov.justice.digital.hmpps.prepEvent
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class ApprovedPremisesServiceTest {
    @Mock lateinit var approvedPremisesApiClient: ApprovedPremisesApiClient
    @Mock lateinit var contactRepository: ContactRepository
    @Mock lateinit var contactTypeRepository: ContactTypeRepository
    @Mock lateinit var contactAlertRepository: ContactAlertRepository
    @Mock lateinit var personRepository: PersonRepository
    @Mock lateinit var personManagerRepository: PersonManagerRepository
    @Mock lateinit var staffRepository: StaffRepository
    @Mock lateinit var teamRepository: TeamRepository
    @InjectMocks lateinit var approvedPremisesService: ApprovedPremisesService

    private val applicationSubmittedEvent = prepEvent("application-submitted", 1234).message
    private val applicationAssessedEvent = prepEvent("application-assessed", 1234).message
    private val bookingMadeEvent = prepEvent("booking-made", 1234).message
    private val personNotArrivedEvent = prepEvent("person-not-arrived", 1234).message

    @Test
    fun `creates alert contact for application submission`() {
        val person = givenAPerson(applicationSubmittedEvent.crn())
        val manager = givenAPersonManager(person)
        val submitter = givenStaff()
        val submittedBy = SubmittedBy(staffMember = StaffMemberGenerator.generate(staffCode = submitter.code))
        val unallocatedTeam = givenUnallocatedTeam()
        val details = givenApplicationSubmittedDetails(submittedBy = submittedBy)
        givenContactTypes(listOf(ContactTypeCode.APPLICATION_SUBMITTED))

        approvedPremisesService.applicationSubmitted(applicationSubmittedEvent)

        verifyAlertContactIsCreated(
            type = ContactTypeCode.APPLICATION_SUBMITTED,
            date = details.eventDetails.submittedAt,
            person = person,
            staff = submitter,
            team = unallocatedTeam,
            alertManager = manager
        )
    }

    @Test
    fun `creates alert contact for application assessment`() {
        val person = givenAPerson(applicationAssessedEvent.crn())
        val manager = givenAPersonManager(person)
        val assessor = givenStaff()
        val unallocatedTeam = givenUnallocatedTeam()
        val details = givenApplicationAssessedDetails(assessedBy = assessor)
        givenContactTypes(listOf(ContactTypeCode.APPLICATION_ASSESSED))

        approvedPremisesService.applicationAssessed(applicationAssessedEvent)

        verifyAlertContactIsCreated(
            type = ContactTypeCode.APPLICATION_ASSESSED,
            date = details.eventDetails.assessedAt,
            person = person,
            staff = assessor,
            team = unallocatedTeam,
            alertManager = manager,
            description = "Approved Premises Application Accepted",
            notes = "Test decision rationale"
        )
    }

    @Test
    fun `creates alert contact for booking made`() {
        val crn = bookingMadeEvent.crn()
        val person = givenAPerson(crn)
        val manager = givenAPersonManager(person)
        val booker = givenStaff()
        val unallocatedTeam = givenUnallocatedTeam()
        val details = givenBookingMadeDetails(bookedBy = booker)
        givenContactTypes(listOf(ContactTypeCode.BOOKING_MADE))

        approvedPremisesService.bookingMade(bookingMadeEvent)

        verifyAlertContactIsCreated(
            type = ContactTypeCode.BOOKING_MADE,
            date = details.eventDetails.createdAt,
            person = person,
            staff = booker,
            team = unallocatedTeam,
            alertManager = manager,
            description = "Approved Premises Booking for Test Premises",
            notes = "To view details of the Approved Premises booking, click here: https://example.com"
        )
    }

    @Test
    fun `creates alert contact for person not arrived`() {
        val crn = personNotArrivedEvent.crn()
        val person = givenAPerson(crn)
        val manager = givenAPersonManager(person)
        val staff = givenStaff()
        val unallocatedTeam = givenUnallocatedTeam()
        val details = givenPersonNotArrivedDetails(recordedBy = staff)
        givenContactTypes(listOf(ContactTypeCode.NOT_ARRIVED))

        approvedPremisesService.personNotArrived(personNotArrivedEvent)

        verifyAlertContactIsCreated(
            type = ContactTypeCode.NOT_ARRIVED,
            date = details.timestamp,
            person = person,
            staff = staff,
            team = unallocatedTeam,
            alertManager = manager,
            notes = """
                TEST
                
                For more details, click here: https://example.com
            """.trimIndent()
        )
    }

    private fun verifyAlertContactIsCreated(
        type: ContactTypeCode,
        date: ZonedDateTime,
        person: Person,
        staff: Staff,
        team: Team,
        alertManager: PersonManager,
        description: String? = null,
        notes: String? = null,
    ) {
        verify(contactRepository).save(
            check { contact ->
                assertThat(contact.type.code, equalTo(type.code))
                assertThat(contact.date, equalTo(date))
                assertThat(contact.startTime, equalTo(date))
                assertThat(contact.person.crn, equalTo(person.crn))
                assertThat(contact.staff, equalTo(staff))
                assertThat(contact.team, equalTo(team))
                assertThat(contact.description, equalTo(description))
                assertThat(contact.notes, equalTo(notes))

                verify(contactAlertRepository).save(
                    check { alert ->
                        assertThat(alert.contactId, equalTo(contact.id))
                        assertThat(alert.typeId, equalTo(contact.type.id))
                        assertThat(alert.personId, equalTo(person.id))
                        assertThat(alert.personManagerId, equalTo(alertManager.id))
                        assertThat(alert.staffId, equalTo(alertManager.staff.id))
                        assertThat(alert.teamId, equalTo(alertManager.team.id))
                    }
                )
            }
        )
    }

    private fun givenAPerson(crn: String): Person {
        val person = PersonGenerator.generate(crn)
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(crn)).thenReturn(person)
        return person
    }

    private fun givenAPersonManager(person: Person): PersonManager {
        val manager = PersonManagerGenerator.generate(person)
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)).thenReturn(manager)
        return manager
    }

    private fun givenStaff(staff: Staff = StaffGenerator.generate()): Staff {
        whenever(staffRepository.findByCode(staff.code)).thenReturn(staff)
        return staff
    }

    private fun givenUnallocatedTeam(
        probationAreaCode: String = ProbationAreaGenerator.DEFAULT.code,
    ): Team {
        val team = TeamGenerator.generate(code = "${probationAreaCode}UAT")
        whenever(teamRepository.findByCodeAndProbationAreaCode("${probationAreaCode}UAT", probationAreaCode))
            .thenReturn(team)
        return team
    }

    private fun givenApplicationSubmittedDetails(
        submittedBy: SubmittedBy = SubmittedBy(staffMember = StaffMemberGenerator.generate())
    ): EventDetails<ApplicationSubmitted> {
        val details = EventDetailsGenerator.applicationSubmitted(submittedBy = submittedBy)
        whenever(approvedPremisesApiClient.getApplicationSubmittedDetails(applicationSubmittedEvent.url())).thenReturn(details)
        return details
    }

    private fun givenApplicationAssessedDetails(
        assessedBy: Staff = StaffGenerator.generate()
    ): EventDetails<ApplicationAssessed> {
        val details = EventDetailsGenerator.applicationAssessed(assessedBy = assessedBy)
        whenever(approvedPremisesApiClient.getApplicationAssessedDetails(applicationAssessedEvent.url())).thenReturn(details)
        return details
    }

    private fun givenBookingMadeDetails(
        bookedBy: Staff = StaffGenerator.generate()
    ): EventDetails<BookingMade> {
        val details = EventDetailsGenerator.bookingMade(bookedBy = bookedBy)
        whenever(approvedPremisesApiClient.getBookingMadeDetails(bookingMadeEvent.url())).thenReturn(details)
        return details
    }

    private fun givenPersonNotArrivedDetails(
        recordedBy: Staff = StaffGenerator.generate()
    ): EventDetails<PersonNotArrived> {
        val details = EventDetailsGenerator.personNotArrived(recordedBy = recordedBy)
        whenever(approvedPremisesApiClient.getPersonNotArrivedDetails(personNotArrivedEvent.url())).thenReturn(details)
        return details
    }

    private fun givenContactTypes(types: List<ContactTypeCode>) {
        whenever(contactRepository.save(any())).thenAnswer { it.arguments[0] }
        types.forEach {
            whenever(contactTypeRepository.findByCode(it.code))
                .thenReturn(ContactTypeGenerator.generate(it.code))
        }
    }
}
