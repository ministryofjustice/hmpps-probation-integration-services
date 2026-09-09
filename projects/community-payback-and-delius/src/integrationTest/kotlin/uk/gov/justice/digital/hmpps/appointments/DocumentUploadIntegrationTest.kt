package uk.gov.justice.digital.hmpps.appointments

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.service.DocumentService

@Transactional
@SpringBootTest
class DocumentUploadIntegrationTest @Autowired constructor(
    private val documentService: DocumentService,
) {
    @Test
    fun `documentService is wired into the Spring context and validateFile is reachable`() {
        documentService.validateFile("smoke-test.pdf", "content".toByteArray())
        assertThrows<IllegalArgumentException> {
            documentService.validateFile("smoke-test.exe", "content".toByteArray())
        }
    }
}
