package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.EventRepository
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.arn.ArnClient
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManager
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManagerRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.common.entity.team.Team
import uk.gov.justice.digital.hmpps.integrations.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.upwassessment.UPWAssessmentService
import uk.gov.justice.digital.hmpps.prepEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@ExtendWith(MockitoExtension::class)
internal class UPWAssessmentServiceTest {

    @Mock
    private lateinit var telemetryService: TelemetryService

    @Mock
    private lateinit var personRepository: PersonWithManagerRepository

    @Mock
    private lateinit var eventRepository: EventRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @InjectMocks
    private lateinit var upwAssessmentService: UPWAssessmentService

    @Mock
    private lateinit var documentService: DocumentService

    @Mock
    private lateinit var arnClient: ArnClient

    @Mock
    private lateinit var staff: Staff

    @Mock
    private lateinit var team: Team

    @Mock
    private lateinit var personManager: PersonManager

    @Mock
    private lateinit var person: PersonWithManager

    @Test
    fun `when person not found`() {
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(CaseGenerator.DEFAULT.crn)).thenReturn(null)
        val notification = prepEvent("upw-assessment-complete")
        upwAssessmentService.processMessage(notification)

        verify(telemetryService).trackEvent(
            "PersonNotFound",
            mapOf("crn" to notification.message.personReference.findCrn()!!)
        )
    }

    @Test
    fun `when event not found`() {
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(CaseGenerator.DEFAULT.crn)).thenReturn(person)
        whenever(eventRepository.existsById(EventGenerator.DEFAULT.id)).thenReturn(false)
        val notification = prepEvent("upw-assessment-complete")

        val exception = assertThrows<NotFoundException> {
            upwAssessmentService.processMessage(notification)
        }
        assertThat(exception.message, equalTo("Event with id of ${EventGenerator.DEFAULT.id} not found"))
    }

    @Test
    fun `when invalid pdf is returned`() {
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(CaseGenerator.DEFAULT.crn)).thenReturn(person)
        whenever(person.crn).thenReturn(CaseGenerator.DEFAULT.crn)
        whenever(person.forename).thenReturn(CaseGenerator.DEFAULT.forename)
        whenever(person.surname).thenReturn(CaseGenerator.DEFAULT.surname)
        whenever(eventRepository.existsById(EventGenerator.DEFAULT.id)).thenReturn(true)
        val notification = prepEvent("upw-assessment-complete", 1234)

        whenever(arnClient.getUPWAssessment(URI(notification.message.detailUrl!!))).thenReturn(
            ResponseEntity.status(HttpStatus.OK)
                .headers { it[HttpHeaders.CONTENT_DISPOSITION] = listOf("filename=upw-assessment.pdf") }
                .body(
                    "Ceci n'est pas une PDF".toByteArray()
                )
        )

        val exception = assertThrows<IllegalStateException> {
            upwAssessmentService.processMessage(notification)
        }
        assertThat(
            exception.message,
            equalTo("Invalid PDF returned for episode: http://localhost:1234/api/upw/download/12345")
        )
    }
}
