package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.CorePersonClient
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.service.EventService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.io.ByteArrayInputStream

@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.cloud.aws.credentials.access-key=test",
        "spring.cloud.aws.credentials.secret-key=test",
        "spring.cloud.aws.sqs.endpoint=http://localhost:\${dev.wiremock.port}"
    ]
)
internal abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var wireMockServer: WireMockServer

    @Value("\${messaging.consumer.queue}")
    protected lateinit var queueName: String

    @MockitoSpyBean
    protected lateinit var telemetryService: TelemetryService

    @MockitoSpyBean
    protected lateinit var auditedInteractionService: AuditedInteractionService

    @MockitoSpyBean
    protected lateinit var personRepository: PersonRepository

    @MockitoSpyBean
    protected lateinit var addressRepository: PersonAddressRepository

    @MockitoSpyBean
    protected lateinit var personManagerRepository: PersonManagerRepository

    @MockitoSpyBean
    protected lateinit var personService: PersonService

    @MockitoSpyBean
    protected lateinit var featureFlags: FeatureFlags

    @MockitoSpyBean
    protected lateinit var eventRepository: EventRepository

    @MockitoSpyBean
    protected lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @MockitoSpyBean
    protected lateinit var contactRepository: ContactRepository

    @MockitoSpyBean
    protected lateinit var courtRepository: CourtRepository

    @MockitoSpyBean
    protected lateinit var mainOffenceRepository: MainOffenceRepository

    @MockitoSpyBean
    protected lateinit var additionalOffenceRepository: AdditionalOffenceRepository

    @MockitoSpyBean
    protected lateinit var orderManagerRepository: OrderManagerRepository

    @MockitoSpyBean
    protected lateinit var eventService: EventService

    @MockitoBean
    protected lateinit var s3Client: S3Client

    @MockitoBean
    protected lateinit var s3AsyncClient: S3AsyncClient

    @MockitoSpyBean
    protected lateinit var corePersonClient: CorePersonClient

    @BeforeEach
    fun setup() {
        doReturn("A111111").whenever(personService).generateCrn()
        whenever(featureFlags.enabled("common-platform-record-creation-toggle")).thenReturn(true)
    }

    protected fun thenNoRecordsAreInserted() {
        verify(orderManagerRepository, never()).save(any())
        verify(courtAppearanceRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(eventRepository, never()).save(any())
        verify(mainOffenceRepository, never()).save(any())
        verify(addressRepository, never()).save(any())
        verify(personRepository, never()).save(any())
        verify(personManagerRepository, never()).save(any())
        verify(auditedInteractionService, never())
            .createAuditedInteraction(any(), any(), eq(AuditedInteraction.Outcome.SUCCESS), any(), anyOrNull())
    }

    protected fun mockOffencePriorityData() {
        val mockCsv = """
            ho_offence_code,offence_desc,priority,offence_type,max_custodial_sentence
            00100,Test offence 1,20,Type,30
            00101,Test offence 2,60,Type,240
            00102,Test offence 3,30,Type,100
        """.trimIndent()
        val request = GetObjectRequest.builder()
            .bucket("offence-priority-bucket")
            .key("offence_priority.csv")
            .build()

        whenever(s3Client.getObject(request)).thenAnswer {
            ResponseInputStream(
                GetObjectResponse.builder().build(),
                ByteArrayInputStream(mockCsv.toByteArray())
            )
        }
    }

    @AfterEach
    fun resetWireMock() {
        wireMockServer.resetAll()
    }

    @AfterEach
    fun cleanup() {
        courtAppearanceRepository.deleteAll()
        additionalOffenceRepository.deleteAll()
        mainOffenceRepository.deleteAll()
        orderManagerRepository.deleteAll()
        eventRepository.deleteAll()
        addressRepository.deleteAll()
        contactRepository.deleteAll()
        personManagerRepository.deleteAll()
        personRepository.deleteAll()
    }
}
