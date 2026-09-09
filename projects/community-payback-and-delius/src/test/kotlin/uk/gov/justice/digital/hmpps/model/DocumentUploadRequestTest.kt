package uk.gov.justice.digital.hmpps.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.web.multipart.MultipartFile

class DocumentUploadRequestTest {
    @Nested
    inner class DataClassBehavior {
        @Test
        fun `create document upload request with file`() {
            val mockFile = mock<MultipartFile>()
            val request = DocumentUploadRequest(file = mockFile)

            assertThat(request.file).isEqualTo(mockFile)
        }

        @Test
        fun `two requests with same file are equal`() {
            val mockFile = mock<MultipartFile>()
            val req1 = DocumentUploadRequest(file = mockFile)
            val req2 = DocumentUploadRequest(file = mockFile)

            assertThat(req1).isEqualTo(req2)
        }

        @Test
        fun `request equals itself`() {
            val mockFile = mock<MultipartFile>()
            val request = DocumentUploadRequest(file = mockFile)

            assertThat(request).isEqualTo(request)
        }

        @Test
        fun `two requests with different files are not equal`() {
            val file1 = mock<MultipartFile>()
            val file2 = mock<MultipartFile>()
            val req1 = DocumentUploadRequest(file = file1)
            val req2 = DocumentUploadRequest(file = file2)

            assertThat(req1).isNotEqualTo(req2)
        }

        @Test
        fun `hashCode is consistent`() {
            val mockFile = mock<MultipartFile>()
            val request = DocumentUploadRequest(file = mockFile)

            assertThat(request.hashCode()).isEqualTo(request.hashCode())
        }

        @Test
        fun `equal requests have same hashCode`() {
            val mockFile = mock<MultipartFile>()
            val req1 = DocumentUploadRequest(file = mockFile)
            val req2 = DocumentUploadRequest(file = mockFile)

            assertThat(req1.hashCode()).isEqualTo(req2.hashCode())
        }

        @Test
        fun `toString contains file reference`() {
            val mockFile = mock<MultipartFile>()
            val request = DocumentUploadRequest(file = mockFile)

            assertThat(request.toString()).isNotEmpty()
                .contains("DocumentUploadRequest")
        }

        @Test
        fun `copy function works`() {
            val mockFile = mock<MultipartFile>()
            val original = DocumentUploadRequest(file = mockFile)
            val copied = original.copy()

            assertThat(copied).isEqualTo(original)
            assertThat(copied).isNotSameAs(original)
        }

        @Test
        fun `destructuring works`() {
            val mockFile = mock<MultipartFile>()
            val request = DocumentUploadRequest(file = mockFile)

            val (extractedFile) = request

            assertThat(extractedFile).isEqualTo(mockFile)
        }

        @Test
        fun `component1 function returns file`() {
            val mockFile = mock<MultipartFile>()
            val request = DocumentUploadRequest(file = mockFile)

            assertThat(request.component1()).isEqualTo(mockFile)
        }
    }
}

class DocumentUploadResponseTest {
    @Nested
    inner class ResponseConstruction {
        @Test
        fun `create response with all fields`() {
            val response = DocumentUploadResponse(
                documentId = 123L,
                filename = "test.pdf",
                alfrescoId = "alfresco-id-456"
            )

            assertThat(response.documentId).isEqualTo(123L)
            assertThat(response.filename).isEqualTo("test.pdf")
            assertThat(response.alfrescoId).isEqualTo("alfresco-id-456")
        }

        @Test
        fun `response equality`() {
            val resp1 = DocumentUploadResponse(
                documentId = 1L,
                filename = "file.pdf",
                alfrescoId = "id1"
            )
            val resp2 = DocumentUploadResponse(
                documentId = 1L,
                filename = "file.pdf",
                alfrescoId = "id1"
            )

            assertThat(resp1).isEqualTo(resp2)
        }

        @Test
        fun `response inequality`() {
            val resp1 = DocumentUploadResponse(
                documentId = 1L,
                filename = "file1.pdf",
                alfrescoId = "id1"
            )
            val resp2 = DocumentUploadResponse(
                documentId = 2L,
                filename = "file2.pdf",
                alfrescoId = "id2"
            )

            assertThat(resp1).isNotEqualTo(resp2)
        }

        @Test
        fun `hashCode consistency`() {
            val response = DocumentUploadResponse(
                documentId = 1L,
                filename = "file.pdf",
                alfrescoId = "id"
            )

            assertThat(response.hashCode()).isEqualTo(response.hashCode())
        }

        @Test
        fun `equal responses same hashCode`() {
            val resp1 = DocumentUploadResponse(1L, "file.pdf", "id")
            val resp2 = DocumentUploadResponse(1L, "file.pdf", "id")

            assertThat(resp1.hashCode()).isEqualTo(resp2.hashCode())
        }

        @Test
        fun `toString representation`() {
            val response = DocumentUploadResponse(123L, "test.pdf", "alfresco-123")

            assertThat(response.toString())
                .contains("DocumentUploadResponse")
                .contains("123")
                .contains("test.pdf")
                .contains("alfresco-123")
        }

        @Test
        fun `copy creates new instance`() {
            val original = DocumentUploadResponse(1L, "file.pdf", "id")
            val copied = original.copy()

            assertThat(copied).isEqualTo(original)
            assertThat(copied).isNotSameAs(original)
        }

        @Test
        fun `copy with modified field`() {
            val original = DocumentUploadResponse(1L, "file.pdf", "id")
            val modified = original.copy(documentId = 999L)

            assertThat(modified.documentId).isEqualTo(999L)
            assertThat(modified.filename).isEqualTo("file.pdf")
            assertThat(original.documentId).isEqualTo(1L)
        }

        @Test
        fun `destructuring response`() {
            val response = DocumentUploadResponse(123L, "test.pdf", "alfresco-id")

            val (id, filename, alfrescoId) = response

            assertThat(id).isEqualTo(123L)
            assertThat(filename).isEqualTo("test.pdf")
            assertThat(alfrescoId).isEqualTo("alfresco-id")
        }

        @Test
        fun `component functions`() {
            val response = DocumentUploadResponse(123L, "test.pdf", "id")

            assertThat(response.component1()).isEqualTo(123L)
            assertThat(response.component2()).isEqualTo("test.pdf")
            assertThat(response.component3()).isEqualTo("id")
        }

        @Test
        fun `different documentId makes responses different`() {
            val resp1 = DocumentUploadResponse(1L, "file.pdf", "id")
            val resp2 = DocumentUploadResponse(2L, "file.pdf", "id")

            assertThat(resp1).isNotEqualTo(resp2)
        }

        @Test
        fun `different filename makes responses different`() {
            val resp1 = DocumentUploadResponse(1L, "file1.pdf", "id")
            val resp2 = DocumentUploadResponse(1L, "file2.pdf", "id")

            assertThat(resp1).isNotEqualTo(resp2)
        }

        @Test
        fun `different alfrescoId makes responses different`() {
            val resp1 = DocumentUploadResponse(1L, "file.pdf", "id1")
            val resp2 = DocumentUploadResponse(1L, "file.pdf", "id2")

            assertThat(resp1).isNotEqualTo(resp2)
        }
    }
}


