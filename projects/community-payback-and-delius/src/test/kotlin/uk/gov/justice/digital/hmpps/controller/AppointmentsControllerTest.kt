package uk.gov.justice.digital.hmpps.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.service.CommunityPaybackAppointmentsService
import uk.gov.justice.digital.hmpps.service.DocumentService

@ExtendWith(MockitoExtension::class)
class AppointmentsControllerTest {

    @Mock
    lateinit var communityPaybackAppointmentsService: CommunityPaybackAppointmentsService

    @Mock
    lateinit var documentService: DocumentService

    @Test
    fun `deleteAppointmentDocument delegates to document service with resolved appointment`() {
        val appointmentId = 1000140L
        val documentId = 7L
        val appointment = mock<UnpaidWorkAppointment>()
        val controller = AppointmentsController(communityPaybackAppointmentsService, documentService)

        whenever(communityPaybackAppointmentsService.getAppointmentForDocumentUpload(appointmentId)).thenReturn(appointment)

        controller.deleteAppointmentDocument(appointmentId, documentId)

        verify(communityPaybackAppointmentsService).getAppointmentForDocumentUpload(appointmentId)
        verify(documentService).deleteDocumentByAppointmentAndDocumentId(appointmentId, documentId, appointment)
    }
}

