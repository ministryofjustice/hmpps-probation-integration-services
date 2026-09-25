package uk.gov.justice.digital.hmpps.appointments

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
) {
    @Test
    fun `documentService is wired into the Spring context and validateFile is reachable`() {
        documentService.validateFile("smoke-test.pdf", "content".toByteArray())
        assertThrows<IllegalArgumentException> {
            documentService.validateFile("smoke-test.exe", "content".toByteArray())
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
    }
}
