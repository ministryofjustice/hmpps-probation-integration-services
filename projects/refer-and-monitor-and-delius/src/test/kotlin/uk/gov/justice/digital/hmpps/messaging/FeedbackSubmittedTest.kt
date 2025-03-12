package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integrations.randm.*
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.service.AppointmentService
import uk.gov.justice.digital.hmpps.service.Attended
import uk.gov.justice.digital.hmpps.service.UpdateAppointmentOutcome
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class FeedbackSubmittedTest {
    @Mock
    lateinit var detailService: DomainEventDetailService

    @Mock
    lateinit var appointmentService: AppointmentService

    @InjectMocks
    lateinit var feedbackSubmitted: FeedbackSubmitted

    private val crn = "T123456"
    private val referralId = UUID.randomUUID()
    private val referralReference = "JS18726AC"
    private val contractTypeName = "Contract Type"
    private val primeProviderName = "Prime Provider"
    private val deliusAppointmentId = 8791827L
    private val domainEvent = HmppsDomainEvent(
        DomainEventType.InitialAppointmentSubmitted.name,
        1,
        "https://interventions-service/referral/$referralId/supplier-assessment",
        additionalInformation = mapOf(
            "serviceUserCRN" to crn,
            "referralId" to referralId.toString(),
            "referralReference" to referralReference,
            "contractTypeName" to contractTypeName,
            "primeProviderName" to primeProviderName,
            "deliusAppointmentId" to deliusAppointmentId.toString(),
            "referralProbationUserURL" to "http://url/pp/$referralId/supplier-assessment"
        ),
        personReference = PersonReference(listOf(PersonIdentifier("CRN", crn)))
    )

    @Test
    fun `Failed result if supplier assessment not found`() {
        val result = feedbackSubmitted.initialAppointmentSubmitted(domainEvent)
        assertThat(result, instanceOf(EventProcessingResult.Failure::class.java))
        val ex = (result as EventProcessingResult.Failure).exception
        assertThat(ex, instanceOf(IllegalArgumentException::class.java))
        assertThat(ex.message, equalTo("Unable to retrieve appointment: ${domainEvent.detailUrl}"))
    }

    @Test
    fun `if appointment not found in supplier assessment, process fails`() {
        val supplierAssessment = SupplierAssessment(UUID.randomUUID(), listOf(), referralId)
        whenever(detailService.getDetail<SupplierAssessment>(anyOrNull(), anyOrNull()))
            .thenReturn(supplierAssessment)

        val result = feedbackSubmitted.initialAppointmentSubmitted(domainEvent)
        assertThat(result, instanceOf(EventProcessingResult.Failure::class.java))
        val ex = (result as EventProcessingResult.Failure).exception
        assertThat(ex, instanceOf(IllegalStateException::class.java))
        assertThat(
            ex.message,
            equalTo("No feedback information available for referral $referralId: supplier assessment ${supplierAssessment.id}")
        )
    }

    @Test
    fun `able to map supplier assessment to appointment outcome`() {
        val appointment =
            Appointment(
                UUID.randomUUID(),
                AppointmentFeedback(
                    AttendanceFeedback(Attended.YES.name, null, ZonedDateTime.now()),
                    SessionFeedback(null, false)
                )
            )
        whenever(detailService.getDetail<SupplierAssessment>(anyOrNull(), anyOrNull())).thenReturn(
            SupplierAssessment(
                UUID.randomUUID(),
                listOf(appointment),
                referralId
            )
        )

        val result = feedbackSubmitted.initialAppointmentSubmitted(domainEvent)
        assertThat(result, instanceOf(EventProcessingResult.Success::class.java))
        assertThat(
            (result as EventProcessingResult.Success).eventType,
            equalTo(DomainEventType.InitialAppointmentSubmitted)
        )
        val maCapture = argumentCaptor<UpdateAppointmentOutcome>()
        verify(appointmentService).updateOutcome(maCapture.capture())
        assertThat(maCapture.firstValue.crn, equalTo(crn))
        assertThat(maCapture.firstValue.referralReference, equalTo(referralReference))
        assertThat(maCapture.firstValue.deliusId, equalTo(deliusAppointmentId))
    }
}
