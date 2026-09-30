package uk.gov.justice.digital.hmpps.appointments

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.model.DocumentUploadResponse
import uk.gov.justice.digital.hmpps.service.DocumentService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class DocumentUploadIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val documentService: DocumentService,
    private val documentRepository: DocumentRepository,
    @Autowired private val wireMockServer: WireMockServer,
) {

    @Test
    fun `documentService is wired into the Spring context and validateFile is reachable`() {
        documentService.validateFile("smoke-test.pdf")
        assertThrows<IllegalArgumentException> {
            documentService.validateFile("smoke-test.exe")
        }
    }

    @Test
    fun `upload document for an appointment`() {
        val appointment = UPWGenerator.DEFAULT_UPW_APPOINTMENT
        val multipartFile = MockMultipartFile(
            "file", "evidence.pdf", "application/pdf", "some document content".toByteArray()
        )

        val response = mockMvc.multipart("/appointments/${appointment.id}/documents") {
            withToken()
            file(multipartFile)
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<DocumentUploadResponse>()

        assertThat(response.filename).isEqualTo("evidence.pdf")
        assertThat(response.alfrescoId).isEqualTo("00000000-0000-0000-0000-000000000001")

        val document = documentRepository.findById(response.documentId).orElseThrow()
        assertThat(document.alfrescoId).isEqualTo("00000000-0000-0000-0000-000000000001")
        assertThat(document.name).isEqualTo("evidence.pdf")
        assertThat(document.tableName).isEqualTo("CONTACT")
        assertThat(document.primaryKeyId).isEqualTo(appointment.contact.id)
        assertThat(document.person.id).isEqualTo(appointment.person.id)
        assertThat(document.createdByUserId).isNotNull
        assertThat(document.lastUpdatedUserId).isEqualTo(document.createdByUserId)

        wireMockServer.verify(
            postRequestedFor(urlEqualTo("/alfresco/uploadnew"))
                .withRequestBodyPart(
                    aMultipart().withName("CRN").withBody(equalTo(appointment.person.crn)).build()
                )
                .withRequestBodyPart(
                    aMultipart().withName("fileName").withBody(equalTo("evidence.pdf")).build()
                )
                .withRequestBodyPart(
                    aMultipart().withFileName("evidence.pdf").build()
                )
                .withRequestBodyPart(
                    aMultipart().withName("entityType").withBody(equalTo("CONTACT")).build()
                )
                .withRequestBodyPart(
                    aMultipart().withName("entityId").withBody(equalTo(appointment.contact.id.toString())).build()
                )
        )
    }
}