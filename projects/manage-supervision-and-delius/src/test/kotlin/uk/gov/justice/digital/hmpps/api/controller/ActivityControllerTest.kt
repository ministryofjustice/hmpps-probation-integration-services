package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivitySearchRequest
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivitySearchResponse
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.service.ActivityService
import uk.gov.justice.digital.hmpps.service.toActivity
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class ActivityControllerTest {

    @Mock
    lateinit var activityService: ActivityService

    @InjectMocks
    lateinit var controller: ActivityController

    private lateinit var personSummary: PersonSummary

    @BeforeEach
    fun setup() {
        personSummary = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN", noms = "NOMS",
            dateOfBirth = LocalDate.now(), offenderId = 1L
        )
    }

    @Test
    fun `calls get get activity function `() {
        val crn = "X000005"
        val expectedResponse = PersonActivity(
            personSummary = personSummary,
            activities = listOfNotNull(
                ContactGenerator.NEXT_APPT_CONTACT.toActivity(),
                ContactGenerator.FIRST_NON_APPT_CONTACT.toActivity(),
                ContactGenerator.FIRST_APPT_CONTACT.toActivity(),
                ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity(),
                ContactGenerator.PREVIOUS_APPT_CONTACT.toActivity()
            )
        )
        whenever(activityService.getPersonActivity(crn)).thenReturn(expectedResponse)
        val res = controller.getPersonActivity(crn)
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `activitySearch calls service with version 1`() {
        val crn = "X000005"
        val searchRequest = PersonActivitySearchRequest(keywords = "test")
        val expectedResponse = PersonActivitySearchResponse(
            size = 10, page = 0, totalResults = 0, totalPages = 0,
            personSummary = personSummary, activities = emptyList()
        )
        whenever(activityService.activitySearch(eq(crn), eq("1"), eq(searchRequest), eq(PageRequest.of(0, 10))))
            .thenReturn(expectedResponse)

        val res = controller.activitySearch(crn, searchRequest, 0, 10)

        assertThat(res, equalTo(expectedResponse))
        verify(activityService).activitySearch(crn, "1", searchRequest, PageRequest.of(0, 10))
    }

    @Test
    fun `activitySearchV2 calls service with version 2`() {
        val crn = "X000005"
        val searchRequest = PersonActivitySearchRequest(keywords = "test")
        val expectedResponse = PersonActivitySearchResponse(
            size = 10, page = 0, totalResults = 0, totalPages = 0,
            personSummary = personSummary, activities = emptyList()
        )
        whenever(activityService.activitySearch(eq(crn), eq("2"), eq(searchRequest), eq(PageRequest.of(0, 10))))
            .thenReturn(expectedResponse)

        val res = controller.activitySearchV2(crn, searchRequest, 0, 10)

        assertThat(res.statusCode, equalTo(HttpStatus.OK))
        assertThat(res.body, equalTo(expectedResponse))
        verify(activityService).activitySearch(crn, "2", searchRequest, PageRequest.of(0, 10))
    }

    @Test
    fun `activitySearchV2 passes back 503`() {
        val crn = "X000005"
        val searchRequest = PersonActivitySearchRequest(keywords = "test")
        val exception = mock<HttpServerErrorException.ServiceUnavailable>()
        whenever(exception.message).thenReturn("test")
        whenever(activityService.activitySearch(eq(crn), eq("2"), eq(searchRequest), eq(PageRequest.of(0, 10))))
            .thenThrow(exception)

        val res = controller.activitySearchV2(crn, searchRequest, 0, 10)

        assertThat(res.statusCode, equalTo(HttpStatus.SERVICE_UNAVAILABLE))
        assertThat(res.body, equalTo(ErrorResponse(503, "test")))
    }

    @Test
    fun `preloadForCrn calls service`() {
        val crn = "X000005"
        val expectedResponse = mapOf("status" to "ok")
        whenever(activityService.preloadForCrn(crn)).thenReturn(expectedResponse)

        val res = controller.preloadForCrn(crn)

        assertThat(res.statusCode, equalTo(HttpStatus.OK))
        verify(activityService).preloadForCrn(crn)
    }

    @Test
    fun `preloadForCrn passes back 503`() {
        val crn = "X000005"
        val exception = mock<HttpServerErrorException.ServiceUnavailable>()
        whenever(exception.message).thenReturn("test")
        whenever(activityService.preloadForCrn(crn)).thenThrow(exception)

        val res = controller.preloadForCrn(crn)

        assertThat(res.statusCode, equalTo(HttpStatus.SERVICE_UNAVAILABLE))
        assertThat(res.body, equalTo(ErrorResponse(503, "test")))
    }
}