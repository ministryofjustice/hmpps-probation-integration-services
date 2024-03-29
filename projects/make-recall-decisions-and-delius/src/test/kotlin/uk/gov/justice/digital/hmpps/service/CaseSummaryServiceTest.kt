package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.ContactTypeSummary
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.custodialEvent
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.nonCustodialEvent
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.release
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.*
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class CaseSummaryServiceTest {
    @Mock
    lateinit var personRepository: CaseSummaryPersonRepository

    @Mock
    lateinit var addressRepository: CaseSummaryAddressRepository

    @Mock
    lateinit var personManagerRepository: CaseSummaryPersonManagerRepository

    @Mock
    lateinit var registrationRepository: CaseSummaryRegistrationRepository

    @Mock
    lateinit var releaseRepository: CaseSummaryReleaseRepository

    @Mock
    lateinit var eventRepository: CaseSummaryEventRepository

    @Mock
    lateinit var contactRepository: CaseSummaryContactRepository

    @InjectMocks
    lateinit var caseSummaryService: CaseSummaryService

    val person = PersonGenerator.CASE_SUMMARY
    val manager = PersonManagerGenerator.CASE_SUMMARY
    val address = AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS

    @Test
    fun `get personal details`() {
        givenPersonalDetails()
        givenAnAddress()
        givenAManager()

        val details = caseSummaryService.getPersonalDetails(person.crn)

        assertThat(details.personalDetails.name.forename, equalTo(person.forename))
        assertThat(details.mainAddress!!.addressNumber, equalTo(address.addressNumber))
        assertThat(details.mainAddress!!.streetName, equalTo(address.streetName))
        assertThat(details.communityManager!!.staffCode, equalTo(manager.staff.code))
        assertThat(details.communityManager!!.team.code, equalTo(manager.team.code))
        assertThat(details.communityManager!!.name.forename, equalTo(manager.staff.forename))
        assertThat(details.communityManager!!.name.surname, equalTo(manager.staff.surname))
    }

    @Test
    fun `get overview`() {
        givenPersonalDetails()
        givenRegistrations()
        val release = givenARelease()

        val overview = caseSummaryService.getOverview(person.crn)

        assertThat(overview.personalDetails.name.forename, equalTo(person.forename))
        assertThat(overview.registerFlags, equalTo(listOf("MAPPA 1", "High RoSH")))
        assertThat(overview.lastRelease!!.releaseDate, equalTo(release.date))
        assertThat(overview.lastRelease!!.recallDate, equalTo(release.recall?.date))
        assertThat(overview.activeConvictions, hasSize(1))
        assertThat(overview.activeConvictions[0].number, equalTo("3"))
        assertThat(overview.activeConvictions[0].mainOffence.description, equalTo("Offence description"))
        assertThat(
            overview.activeConvictions[0].additionalOffences[0].description,
            equalTo("Additional offence description")
        )
        assertThat(overview.activeConvictions[0].sentence!!.description, equalTo("Sentence type"))
        assertThat(overview.activeConvictions[0].sentence!!.length, equalTo(6))
        assertThat(overview.activeConvictions[0].sentence!!.lengthUnits, equalTo("Months"))
        assertThat(overview.activeConvictions[0].sentence!!.isCustodial, equalTo(true))
        assertThat(overview.activeConvictions[0].sentence!!.custodialStatusCode, equalTo("B"))
        assertThat(overview.activeConvictions[0].sentence!!.sentenceExpiryDate, equalTo(LocalDate.of(2023, 1, 1)))
        assertThat(overview.activeConvictions[0].sentence!!.licenceExpiryDate, equalTo(LocalDate.of(2024, 1, 1)))
    }

    @Test
    fun `get overview returns most recent release date from any active custodial event`() {
        val events = listOf(custodialEvent(person.id), custodialEvent(person.id), nonCustodialEvent(person.id))
        val release1 = events[0].disposal!!.custody!!.release(LocalDate.of(2020, 1, 1))
        val release2 = events[1].disposal!!.custody!!.release(LocalDate.of(2020, 1, 2))

        givenPersonalDetails()
        givenEvents(events)
        givenReleases(listOf(release1, release2))

        val overview = caseSummaryService.getOverview(person.crn)

        assertThat(overview.activeConvictions, hasSize(3))
        assertThat(overview.lastRelease?.releaseDate, equalTo(LocalDate.of(2020, 1, 2)))
    }

    @Test
    fun `get overview handles no custodial events`() {
        givenPersonalDetails()
        givenEvents(emptyList())

        val overview = caseSummaryService.getOverview(person.crn)

        assertThat(overview.activeConvictions, empty())
        assertThat(overview.lastRelease, nullValue())
    }

    @Test
    fun `get mappa and rosh history`() {
        givenPersonalDetails()
        givenMappa()
        givenRosh()

        val overview = caseSummaryService.getMappaAndRoshHistory(person.crn)

        assertThat(overview.personalDetails.name.forename, equalTo(person.forename))
        assertThat(overview.mappa!!.category, equalTo(1))
        assertThat(overview.mappa!!.level, equalTo(2))
        assertThat(overview.mappa!!.startDate, equalTo(RegistrationGenerator.MAPPA.date))
        assertThat(overview.roshHistory, hasSize(1))
        assertThat(overview.roshHistory[0].type, equalTo(RegistrationGenerator.HIGH_ROSH.type.code))
        assertThat(overview.roshHistory[0].typeDescription, equalTo(RegistrationGenerator.HIGH_ROSH.type.description))
        assertThat(overview.roshHistory[0].notes, equalTo(RegistrationGenerator.HIGH_ROSH.notes))
        assertThat(overview.roshHistory[0].startDate, equalTo(RegistrationGenerator.HIGH_ROSH.date))
    }

    @Test
    fun `unexpected mappa category is thrown`() {
        givenPersonalDetails()
        givenMappa(RegistrationGenerator.generate(person.id, RegisterType.MAPPA_TYPE, "MAPPA", category = "XX"))

        val exception = assertThrows<IllegalStateException> {
            caseSummaryService.getMappaAndRoshHistory(person.crn)
        }

        assertThat(exception.message, equalTo("Unexpected MAPPA category: XX"))
    }

    @Test
    fun `unexpected mappa level is thrown`() {
        givenPersonalDetails()
        givenMappa(RegistrationGenerator.generate(person.id, RegisterType.MAPPA_TYPE, "MAPPA", level = "YY"))

        val exception = assertThrows<IllegalStateException> {
            caseSummaryService.getMappaAndRoshHistory(person.crn)
        }

        assertThat(exception.message, equalTo("Unexpected MAPPA level: YY"))
    }

    @Test
    fun `get contact history`() {
        givenPersonalDetails()
        givenContacts()

        val contacts = caseSummaryService.getContactHistory(person.crn)

        assertThat(contacts.personalDetails.name.forename, equalTo(person.forename))
        assertThat(contacts.contacts.size, equalTo(2))
        assertThat(contacts.contacts[0].type.systemGenerated, equalTo(false))
        assertThat(contacts.contacts[1].type.systemGenerated, equalTo(true))
        assertThat(contacts.summary.types.size, equalTo(1))
        assertThat(contacts.summary.types[0].code, equalTo("TYPE"))
        assertThat(contacts.summary.types[0].total, equalTo(123))
    }

    @Test
    fun `handles missing contact start time`() {
        givenPersonalDetails()
        givenContacts()
        whenever(contactRepository.findContacts(person.id, null, LocalDate.now(), true, emptyList(), 0))
            .thenReturn(listOf(ContactGenerator.generate(date = LocalDate.of(2023, 1, 1), time = null)))

        val contacts = caseSummaryService.getContactHistory(person.crn)

        assertThat(contacts.contacts.size, equalTo(1))
        assertThat(contacts.contacts[0].startDateTime, equalTo(ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, EuropeLondon)))
    }

    @Test
    fun `get recommendation model`() {
        givenPersonalDetails()
        givenARelease()

        val recommendationModel = caseSummaryService.getRecommendationModel(person.crn)

        assertThat(recommendationModel.lastReleasedFromInstitution!!.name, equalTo("Test institution"))
        assertThat(recommendationModel.activeCustodialConvictions, hasSize(1))
        assertThat(recommendationModel.activeCustodialConvictions[0].sentence!!.secondLength, equalTo(2))
        assertThat(recommendationModel.activeCustodialConvictions[0].sentence!!.secondLengthUnits, equalTo("Years"))
        assertThat(
            recommendationModel.activeCustodialConvictions[0].sentence!!.startDate,
            equalTo(LocalDate.of(2021, 1, 1))
        )
    }

    private fun givenPersonalDetails() {
        whenever(personRepository.findByCrn(person.crn))
            .thenReturn(person)
    }

    private fun givenAnAddress() {
        whenever(addressRepository.findMainAddress(person.id)).thenReturn(address)
    }

    private fun givenAManager() {
        whenever(personManagerRepository.findByPersonId(person.id)).thenReturn(PersonManagerGenerator.CASE_SUMMARY)
    }

    private fun givenRegistrations(
        registrations: List<Registration> = listOf(RegistrationGenerator.MAPPA, RegistrationGenerator.HIGH_ROSH)
    ) {
        whenever(registrationRepository.findActiveTypeDescriptionsByPersonId(person.id))
            .thenReturn(registrations.map { it.type.description })
    }

    private fun givenMappa(mappa: Registration = RegistrationGenerator.MAPPA) {
        whenever(
            registrationRepository.findFirstByPersonIdAndTypeCodeAndDeregisteredFalseOrderByDateDesc(
                person.id,
                RegisterType.MAPPA_TYPE
            )
        )
            .thenReturn(mappa)
    }

    private fun givenRosh(rosh: List<Registration> = listOf(RegistrationGenerator.HIGH_ROSH)) {
        whenever(registrationRepository.findByPersonIdAndTypeFlagCodeOrderByDateDesc(person.id, RegisterType.ROSH_FLAG))
            .thenReturn(rosh)
    }

    private fun givenEvents(events: List<Event>): List<Event> {
        whenever(eventRepository.findByPersonId(person.id)).thenReturn(events)
        return events
    }

    private fun givenACustodialEvent(event: Event = EventGenerator.CASE_SUMMARY) =
        givenEvents(listOf(event))[0]

    private fun givenARelease(release: Release = givenACustodialEvent().disposal!!.custody!!.release()) =
        givenReleases(listOf(release))[0]

    private fun givenReleases(releases: List<Release> = listOf(givenACustodialEvent().disposal!!.custody!!.release())): List<Release> {
        whenever(releaseRepository.findFirstByCustodyIdInOrderByDateDesc(releases.map { it.custodyId }))
            .thenReturn(releases.maxBy { it.date })
        return releases
    }

    private fun givenContacts() {
        whenever(contactRepository.findContacts(person.id, null, LocalDate.now(), true, emptyList(), 0))
            .thenReturn(listOf(ContactGenerator.DEFAULT, ContactGenerator.SYSTEM_GENERATED))
        whenever(contactRepository.summarizeContactTypes(person.id))
            .thenReturn(listOf(ContactTypeSummary("TYPE", "Description", 123)))
    }
}
