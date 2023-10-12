package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.whenever
import org.springframework.boot.context.event.ApplicationStartedEvent
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.AssessedByGenerator
import uk.gov.justice.digital.hmpps.data.generator.BookedByGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventDetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffMemberGenerator
import uk.gov.justice.digital.hmpps.data.generator.SubmittedByGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationAssessed
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationSubmitted
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.AssessedBy
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookedBy
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookingMade
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonNotArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.SubmittedBy
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.MoveOnCategoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Referral
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralSourceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ResidenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.location.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.TransferReason
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.DatasetCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url
import uk.gov.justice.digital.hmpps.prepEvent
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.AuditUserService
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class ApprovedPremisesServiceTest {
    @Mock
    lateinit var approvedPremisesApiClient: ApprovedPremisesApiClient

    @Mock
    lateinit var approvedPremisesRepository: ApprovedPremisesRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var contactOutcomeRepository: ContactOutcomeRepository

    @Mock
    lateinit var officeLocationRepository: OfficeLocationRepository

    @Mock
    lateinit var contactAlertRepository: ContactAlertRepository

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var personAddressRepository: PersonAddressRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var nsiRepository: NsiRepository

    @Mock
    lateinit var nsiTypeRepository: NsiTypeRepository

    @Mock
    lateinit var nsiStatusRepository: NsiStatusRepository

    @Mock
    lateinit var nsiManagerRepository: NsiManagerRepository

    @Mock
    lateinit var transferReasonRepository: TransferReasonRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var referralSourceRepository: ReferralSourceRepository

    @Mock
    lateinit var moveOnCategoryRepository: MoveOnCategoryRepository

    @Mock
    lateinit var referralRepository: ReferralRepository

    @Mock
    lateinit var residenceRepository: ResidenceRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @Mock
    lateinit var auditUserService: AuditUserService

    @Mock
    lateinit var applicationStartedEvent: ApplicationStartedEvent

    lateinit var addressService: AddressService
    lateinit var contactService: ContactService
    lateinit var nsiService: NsiService
    lateinit var referralService: ReferralService
    lateinit var approvedPremisesService: ApprovedPremisesService

    private val applicationSubmittedEvent = prepEvent("application-submitted").message
    private val applicationAssessedEvent = prepEvent("application-assessed").message
    private val bookingMadeEvent = prepEvent("booking-made").message
    private val personNotArrivedEvent = prepEvent("person-not-arrived").message
    private val personArrivedEvent = prepEvent("person-arrived").message

    @BeforeEach
    fun setup() {
        addressService = AddressService(personAddressRepository, referenceDataRepository)
        contactService = ContactService(
            contactRepository,
            contactTypeRepository,
            contactOutcomeRepository,
            contactAlertRepository,
            officeLocationRepository,
            teamRepository,
            personManagerRepository
        )
        referralService = ReferralService(
            referenceDataRepository,
            referralSourceRepository,
            moveOnCategoryRepository,
            teamRepository,
            staffRepository,
            referralRepository,
            residenceRepository,
            personRepository,
            eventRepository,
            registrationRepository,
            contactService
        )
        nsiService = NsiService(
            nsiRepository,
            nsiTypeRepository,
            nsiStatusRepository,
            nsiManagerRepository,
            personRepository,
            teamRepository,
            staffRepository,
            transferReasonRepository,
            addressService,
            contactService,
            referralService,
            referenceDataRepository,
            eventRepository
        )
        approvedPremisesService = ApprovedPremisesService(
            approvedPremisesApiClient,
            approvedPremisesRepository,
            staffRepository,
            personRepository,
            eventRepository,
            contactService,
            nsiService,
            referralService
        )
    }

    @Test
    fun `creates alert contact for application submission`() {
        val person = givenAPerson(applicationSubmittedEvent.crn())
        val manager = givenAPersonManager(person)
        givenAnEvent(person, "3")
        val submitter = givenStaff()
        val submittedBy = SubmittedByGenerator.generate(
            staffMember = StaffMemberGenerator.generate(staffCode = submitter.code)
        )
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
            alertManager = manager,
            notes = details.eventDetails.notes,
            description = "Approved Premises Application Submitted"
        )
    }

    @Test
    fun `creates alert contact for application assessment`() {
        val person = givenAPerson(applicationAssessedEvent.crn())
        val manager = givenAPersonManager(person)
        givenAnEvent(person, "7")
        val assessor = givenStaff()
        val unallocatedTeam = givenUnallocatedTeam()
        val assessedBy =
            AssessedByGenerator.generate(staffMember = StaffMemberGenerator.generate(staffCode = assessor.code))
        val details = givenApplicationAssessedDetails(assessedBy = assessedBy)
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
            notes = details.eventDetails.notes
        )
    }

    @Test
    fun `throws not found when approved premises not found`() {
        val bookedBy = BookedByGenerator.generate()
        givenBookingMadeDetails(bookedBy = bookedBy)

        val ex = assertThrows<NotFoundException> { approvedPremisesService.bookingMade(bookingMadeEvent) }
        assertThat(ex.message, equalTo("Approved Premises with code of Q001 not found"))
    }

    @Test
    fun `creates alert contact for person not arrived`() {
        val crn = personNotArrivedEvent.crn()
        val person = givenAPerson(crn)
        val manager = givenAPersonManager(person)
        val staff = givenStaff()
        val unallocatedTeam = givenUnallocatedTeam()
        val details = givenPersonNotArrivedDetails(recordedBy = staff)
        givenAnApprovedPremises(ApprovedPremisesGenerator.DEFAULT)
        givenContactTypes(listOf(ContactTypeCode.NOT_ARRIVED))
        givenAuditUser()
        givenReferral(person, details.eventDetails.bookingId)

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
            """.trimIndent(),
            description = "The reason they didn't attend"
        )
    }

    @Test
    fun `creates alert contact and residence NSI when person arrives`() {
        val crn = personArrivedEvent.crn()
        val person = givenAPerson(crn)
        val manager = givenAPersonManager(person)
        givenAnEvent(person, "11")
        val staff = givenStaff()
        val approvedPremisesTeam = givenApprovedPremisesTeam()
        val details = givenPersonArrivedDetails(keyWorker = staff)
        givenContactTypes(listOf(ContactTypeCode.ARRIVED))
        givenNsiTypes(listOf(NsiTypeCode.APPROVED_PREMISES_RESIDENCE), listOf(NsiStatusCode.IN_RESIDENCE))
        givenAnApprovedPremises(ApprovedPremisesGenerator.DEFAULT)
        givenAddressStatuses(listOf(ReferenceDataGenerator.MAIN_ADDRESS_STATUS))
        givenAddressTypes(listOf(ReferenceDataGenerator.AP_ADDRESS_TYPE))
        givenAuditUser()
        givenReferral(person, details.eventDetails.bookingId)

        approvedPremisesService.personArrived(personArrivedEvent)

        verifyAlertContactIsCreated(
            type = ContactTypeCode.ARRIVED,
            date = details.eventDetails.arrivedAt,
            person = person,
            staff = staff,
            team = approvedPremisesTeam,
            alertManager = manager,
            notes = """
                Arrived on time
                
                For more details, click here: https://example.com
            """.trimIndent(),
            description = "Arrived at Test Premises"
        )
        verifyNsiIsCreated(
            type = NsiTypeCode.APPROVED_PREMISES_RESIDENCE,
            status = NsiStatusCode.IN_RESIDENCE,
            referralDate = details.eventDetails.applicationSubmittedOn,
            actualStartDate = details.eventDetails.arrivedAt,
            expectedStartDate = details.eventDetails.arrivedAt.toLocalDate(),
            expectedEndDate = details.eventDetails.expectedDepartureOn,
            person = person,
            staff = staff,
            team = approvedPremisesTeam,
            notes = """
                Arrived on time
                
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
        notes: String? = null
    ) {
        verify(contactRepository).save(
            check { contact ->
                assertThat(contact.type.code, equalTo(type.code))
                assertThat(contact.date, equalTo(date.toLocalDate()))
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

    private fun verifyNsiIsCreated(
        type: NsiTypeCode,
        status: NsiStatusCode,
        referralDate: LocalDate,
        actualStartDate: ZonedDateTime?,
        expectedStartDate: LocalDate?,
        expectedEndDate: LocalDate?,
        person: Person,
        staff: Staff,
        team: Team,
        notes: String? = null
    ) {
        verify(nsiRepository).save(
            check { nsi ->
                assertThat(nsi.type.code, equalTo(type.code))
                assertThat(nsi.status.code, equalTo(status.code))
                assertThat(nsi.referralDate, equalTo(referralDate))
                assertThat(nsi.expectedStartDate, equalTo(expectedStartDate))
                assertThat(nsi.expectedEndDate, equalTo(expectedEndDate))
                assertThat(nsi.actualStartDate, equalTo(actualStartDate))
                assertThat(nsi.person.crn, equalTo(person.crn))
                assertThat(nsi.notes, equalTo(notes))

                verify(nsiManagerRepository).save(
                    check { manager ->
                        assertThat(manager.nsi, equalTo(nsi))
                        assertThat(manager.staff, equalTo(staff))
                        assertThat(manager.team, equalTo(team))
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
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)).thenReturn(
            manager
        )
        return manager
    }

    private fun givenAnEvent(person: Person, eventNumber: String): Event {
        val event = PersonGenerator.generateEvent(eventNumber, person.id)
        whenever(eventRepository.findByPersonIdAndNumber(person.id, eventNumber))
            .thenReturn(event)
        return event
    }

    private fun givenStaff(staff: Staff = StaffGenerator.generate()): Staff {
        whenever(staffRepository.findByCode(staff.code)).thenReturn(staff)
        return staff
    }

    private fun givenUnallocatedTeam(
        probationAreaCode: String = ProbationAreaGenerator.DEFAULT.code
    ): Team {
        val team = TeamGenerator.generate(code = "${probationAreaCode}UAT")
        whenever(teamRepository.findByCodeAndProbationAreaCode("${probationAreaCode}UAT", probationAreaCode))
            .thenReturn(team)
        return team
    }

    private fun givenApprovedPremisesTeam(team: Team = TeamGenerator.APPROVED_PREMISES_TEAM): Team {
        whenever(teamRepository.findByApprovedPremisesCodeCode(team.approvedPremises!!.code.code)).thenReturn(team)
        return team
    }

    private fun givenApplicationSubmittedDetails(
        submittedBy: SubmittedBy = SubmittedByGenerator.generate()
    ): EventDetails<ApplicationSubmitted> {
        val details = EventDetailsGenerator.applicationSubmitted(submittedBy = submittedBy)
        whenever(approvedPremisesApiClient.getApplicationSubmittedDetails(applicationSubmittedEvent.url())).thenReturn(
            details
        )
        return details
    }

    private fun givenApplicationAssessedDetails(
        assessedBy: AssessedBy = AssessedByGenerator.generate()
    ): EventDetails<ApplicationAssessed> {
        val details = EventDetailsGenerator.applicationAssessed(assessedBy = assessedBy)
        whenever(approvedPremisesApiClient.getApplicationAssessedDetails(applicationAssessedEvent.url())).thenReturn(
            details
        )
        return details
    }

    private fun givenBookingMadeDetails(
        bookedBy: BookedBy = BookedByGenerator.generate()
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

    private fun givenPersonArrivedDetails(
        keyWorker: Staff = StaffGenerator.generate()
    ): EventDetails<PersonArrived> {
        val details = EventDetailsGenerator.personArrived(keyWorker = keyWorker)
        whenever(approvedPremisesApiClient.getPersonArrivedDetails(personArrivedEvent.url())).thenReturn(details)
        return details
    }

    private fun givenContactTypes(types: List<ContactTypeCode>) {
        whenever(contactRepository.save(any())).thenAnswer { it.arguments[0] }
        types.forEach {
            whenever(contactTypeRepository.findByCode(it.code)).thenReturn(ContactTypeGenerator.generate(it.code))
        }
    }

    private fun givenAnApprovedPremises(ap: ApprovedPremises) {
        whenever(approvedPremisesRepository.findByCodeCodeAndSelectable(ap.code.code)).thenReturn(ap)
    }

    private fun givenAddressStatuses(statuses: List<ReferenceData>) {
        statuses.forEach {
            whenever(referenceDataRepository.findByCodeAndDatasetCode(it.code, DatasetCode.ADDRESS_STATUS))
                .thenReturn(it)
        }
    }

    private fun givenAddressTypes(types: List<ReferenceData>) {
        types.forEach {
            whenever(referenceDataRepository.findByCodeAndDatasetCode(it.code, DatasetCode.ADDRESS_TYPE))
                .thenReturn(it)
        }
    }

    private fun givenNsiTypes(types: List<NsiTypeCode> = listOf(), statuses: List<NsiStatusCode> = listOf()) {
        whenever(nsiRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(nsiManagerRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(transferReasonRepository.findByCode("NSI"))
            .thenReturn(TransferReason(IdGenerator.getAndIncrement(), "NSI"))
        types.forEach {
            whenever(nsiTypeRepository.findByCode(it.code)).thenReturn(NsiType(IdGenerator.getAndIncrement(), it.code, "description of ${it.code}"))
        }
        statuses.forEach {
            whenever(nsiStatusRepository.findByCode(it.code))
                .thenReturn(NsiStatus(IdGenerator.getAndIncrement(), it.code))
        }
    }

    private fun givenAuditUser() {
        val user = UserGenerator.AUDIT_USER
        whenever(auditUserService.findUser(user.username)).thenReturn(user)
        ServiceContext(user.username, auditUserService).onApplicationEvent(applicationStartedEvent)
    }

    private fun givenReferral(person: Person, bookingId: String): Referral {
        val ref = Referral(
            person.id,
            564,
            ApprovedPremisesGenerator.DEFAULT.id,
            LocalDate.now(),
            LocalDate.now(),
            LocalDate.now(),
            ZonedDateTime.now(),
            Nsi.EXT_REF_BOOKING_PREFIX + bookingId,
            ReferenceDataGenerator.REFERRAL_DATE_TYPE.id,
            ReferenceDataGenerator.OTHER_REFERRAL_CATEGORY.id,
            1, 1,
            "Reason",
            ReferenceDataGenerator.OTHER_REFERRAL_SOURCE.id,
            ReferenceDataGenerator.AP_REFERRAL_SOURCE.id,
            ReferenceDataGenerator.ACCEPTED_DEFERRED_ADMISSION.id,
            1, 1, null,
            ReferenceDataGenerator.YN_UNKNOWN.id, null,
            ReferenceDataGenerator.YN_UNKNOWN.id, null,
            ReferenceDataGenerator.YN_UNKNOWN.id, null,
            true, true,
            ReferenceDataGenerator.RISK_UNKNOWN.id,
            ReferenceDataGenerator.RISK_UNKNOWN.id,
            ReferenceDataGenerator.RISK_UNKNOWN.id,
            ReferenceDataGenerator.RISK_UNKNOWN.id,
            ReferenceDataGenerator.RISK_UNKNOWN.id,
            ReferenceDataGenerator.RISK_UNKNOWN.id,
            ReferenceDataGenerator.RISK_UNKNOWN.id,
            null
        )
        whenever(
            referralRepository.findByPersonIdAndCreatedByUserIdAndReferralNotesContains(
                person.id,
                UserGenerator.AUDIT_USER.id,
                Nsi.EXT_REF_BOOKING_PREFIX + bookingId
            )
        ).thenReturn(ref)
        return ref
    }
}
