package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.integrations.arn.ArnClient
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.upwassessment.UPWAssessmentService
import uk.gov.justice.digital.hmpps.prepMessage
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class UPWAssessmentServiceTest {

    @Mock
    private lateinit var telemetryService: TelemetryService

    @Mock
    private lateinit var personRepository: PersonWithManagerRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @InjectMocks
    private lateinit var upwAssessmentService: UPWAssessmentService

    @Mock
    private lateinit var documentService: DocumentService

    @Mock
    private lateinit var personWithManagerRepository: PersonWithManagerRepository

    @Mock
    private lateinit var arnClient: ArnClient

    @Test
    fun `when person not found exception thrown`() {
        whenever(personRepository.findByCrnAndSoftDeletedIsFalse(CaseGenerator.DEFAULT.crn)).thenReturn(null)
        val notification = prepMessage("upw-assessment-complete")
        upwAssessmentService.processMessage(notification)

        verify(telemetryService).trackEvent(
            "PersonNotFound",
            mapOf("crn" to notification.message.personReference.findCrn()!!)
        )
    }
}
