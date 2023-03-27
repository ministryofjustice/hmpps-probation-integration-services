package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.release
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryRegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryReleaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Release
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.findMainAddress
import java.time.LocalDate

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

    @InjectMocks
    lateinit var caseSummaryService: CaseSummaryService

    @Test
    fun `get personal details`() {
        givenPersonalDetails()

        val personalDetails = caseSummaryService.getPersonalDetails(PersonGenerator.CASE_SUMMARY.crn)

        assertThat(personalDetails.name.forename, equalTo(PersonGenerator.CASE_SUMMARY.forename))
        assertThat(personalDetails.mainAddress!!.streetName, equalTo(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS.streetName))
    }

    @Test
    fun `get overview`() {
        givenPersonalDetails()
        givenAManager()
        givenRegistrations()
        val release = givenARelease()

        val overview = caseSummaryService.getOverview(PersonGenerator.CASE_SUMMARY.crn)

        assertThat(overview.personalDetails.name.forename, equalTo(PersonGenerator.CASE_SUMMARY.forename))
        assertThat(overview.personalDetails.mainAddress!!.addressNumber, equalTo(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS.addressNumber))
        assertThat(overview.personalDetails.mainAddress!!.streetName, equalTo(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS.streetName))
        assertThat(overview.communityManager!!.staffCode, equalTo("STAFF01"))
        assertThat(overview.communityManager!!.team.code, equalTo("TEAM01"))
        assertThat(overview.communityManager!!.name.forename, equalTo("Forename"))
        assertThat(overview.communityManager!!.name.surname, equalTo("Surname"))
        assertThat(overview.registerFlags, equalTo(listOf("MAPPA 1", "High RoSH")))
        assertThat(overview.lastRelease!!.releaseDate, equalTo(release.date))
        assertThat(overview.lastRelease!!.recallDate, equalTo(release.recall?.date))
        assertThat(overview.activeConvictions, hasSize(1))
        assertThat(overview.activeConvictions[0].number, equalTo("3"))
        assertThat(overview.activeConvictions[0].mainOffence, equalTo("Offence description"))
        assertThat(overview.activeConvictions[0].additionalOffences[0], equalTo("Additional offence description"))
        assertThat(overview.activeConvictions[0].sentence!!.description, equalTo("Sentence type"))
        assertThat(overview.activeConvictions[0].sentence!!.length, equalTo(6))
        assertThat(overview.activeConvictions[0].sentence!!.lengthUnits, equalTo("Months"))
        assertThat(overview.activeConvictions[0].sentence!!.isCustodial, equalTo(true))
        assertThat(overview.activeConvictions[0].sentence!!.custodialStatusCode, equalTo("B"))
        assertThat(overview.activeConvictions[0].sentence!!.sentenceExpiryDate, equalTo(LocalDate.of(2023, 1, 1)))
        assertThat(overview.activeConvictions[0].sentence!!.licenceExpiryDate, equalTo(LocalDate.of(2024, 1, 1)))
    }

    @Test
    fun `get overview throws when multiple custodial events`() {
        givenPersonalDetails()
        givenCustodialEvents(List(3) { EventGenerator.custodialEvent(PersonGenerator.CASE_SUMMARY.id) })

        val exception = assertThrows<IllegalStateException> {
            caseSummaryService.getOverview(PersonGenerator.CASE_SUMMARY.crn)
        }
        assertThat(exception.message, equalTo("Multiple active custodial events for X000004"))
    }

    @Test
    fun `get overview throws when no custodial events`() {
        givenPersonalDetails()
        givenCustodialEvents(emptyList())

        val exception = assertThrows<IllegalStateException> {
            caseSummaryService.getOverview(PersonGenerator.CASE_SUMMARY.crn)
        }
        assertThat(exception.message, equalTo("No active custodial events for X000004"))
    }

    private fun givenPersonalDetails() {
        whenever(personRepository.findByCrn(PersonGenerator.CASE_SUMMARY.crn)).thenReturn(PersonGenerator.CASE_SUMMARY)
        whenever(addressRepository.findMainAddress(PersonGenerator.CASE_SUMMARY.id)).thenReturn(AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS)
    }

    private fun givenAManager() {
        whenever(personManagerRepository.findByPersonId(PersonGenerator.CASE_SUMMARY.id))
            .thenReturn(PersonManagerGenerator.CASE_SUMMARY)
    }

    private fun givenRegistrations(
        registrations: List<Registration> = listOf(RegistrationGenerator.MAPPA, RegistrationGenerator.HIGH_ROSH)
    ) {
        whenever(registrationRepository.findTypeDescriptionsByPersonId(PersonGenerator.CASE_SUMMARY.id))
            .thenReturn(registrations.map { it.type.description })
    }

    private fun givenCustodialEvents(events: List<Event>): List<Event> {
        whenever(eventRepository.findByPersonId(PersonGenerator.CASE_SUMMARY.id)).thenReturn(events)
        return events
    }

    private fun givenACustodialEvent(event: Event = EventGenerator.CASE_SUMMARY) =
        givenCustodialEvents(listOf(event))[0]

    private fun givenARelease(): Release {
        val event = givenACustodialEvent()
        val release = event.disposal!!.custody!!.release()
        whenever(releaseRepository.findFirstByCustodyIdOrderByDateDesc(event.disposal!!.custody!!.id)).thenReturn(release)
        return release
    }
}
