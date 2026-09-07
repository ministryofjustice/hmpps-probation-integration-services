package uk.gov.justice.digital.hmpps.appointments

import org.junit.jupiter.api.Nested
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
    private val testFileContent = "Test file content".toByteArray()

    @Nested
    inner class FileValidation {
        @Test
        fun `reject exe files`() {
            assertThrows<IllegalArgumentException> {
                documentService.validateFile("malicious.exe", testFileContent)
            }
        }

        @Test
        fun `reject zip files`() {
            assertThrows<IllegalArgumentException> {
                documentService.validateFile("archive.zip", testFileContent)
            }
        }

        @Test
        fun `reject file with no extension`() {
            assertThrows<IllegalArgumentException> {
                documentService.validateFile("no-extension", testFileContent)
            }
        }

        @Test
        fun `accept uppercase PDF`() {
            documentService.validateFile("document.PDF", testFileContent)
        }

        @Test
        fun `accept lowercase pdf`() {
            documentService.validateFile("document.pdf", testFileContent)
        }

        @Test
        fun `accept doc`() {
            documentService.validateFile("report.doc", testFileContent)
        }

        @Test
        fun `accept docx`() {
            documentService.validateFile("report.docx", testFileContent)
        }

        @Test
        fun `accept xlsx`() {
            documentService.validateFile("data.xlsx", testFileContent)
        }

        @Test
        fun `accept csv`() {
            documentService.validateFile("report.csv", testFileContent)
        }

        @Test
        fun `accept jpg`() {
            documentService.validateFile("photo.jpg", testFileContent)
        }

        @Test
        fun `accept png`() {
            documentService.validateFile("screenshot.png", testFileContent)
        }

        @Test
        fun `accept mp3`() {
            documentService.validateFile("song.mp3", testFileContent)
        }

        @Test
        fun `reject html`() {
            assertThrows<IllegalArgumentException> {
                documentService.validateFile("webpage.html", testFileContent)
            }
        }

        @Test
        fun `reject js`() {
            assertThrows<IllegalArgumentException> {
                documentService.validateFile("script.js", testFileContent)
            }
        }

        @Test
        fun `reject bat`() {
            assertThrows<IllegalArgumentException> {
                documentService.validateFile("script.bat", testFileContent)
            }
        }

        @Test
        fun `handle special characters`() {
            documentService.validateFile("certificate-2026-09-05 (final).pdf", testFileContent)
        }

        @Test
        fun `handle multiple dots`() {
            documentService.validateFile("report.draft.final.pdf", testFileContent)
        }

        @Test
        fun `handle long filename`() {
            val longName = "a".repeat(200) + ".pdf"
            documentService.validateFile(longName, testFileContent)
        }
    }

    @Nested
    inner class AllSupportedFormats {
        @Test
        fun `doc formats`() {
            val docFormats =
                listOf("doc", "docx", "rtf", "txt", "dot", "dotm", "docm", "odt", "xml", "wpd", "wri", "wps")
            docFormats.forEach { ext ->
                documentService.validateFile("test.$ext", testFileContent)
            }
        }

        @Test
        fun `sheets`() {
            val sheets = listOf("xls", "xlsb", "xlsx", "csv")
            sheets.forEach { ext ->
                documentService.validateFile("test.$ext", testFileContent)
            }
        }

        @Test
        fun `images`() {
            val images = listOf("bmp", "jpg", "jpeg", "gif", "png")
            images.forEach { ext ->
                documentService.validateFile("test.$ext", testFileContent)
            }
        }

        @Test
        fun `audio`() {
            val audio = listOf("m4a", "flac", "mp3", "mp4", "wav", "wma", "aac")
            audio.forEach { ext ->
                documentService.validateFile("test.$ext", testFileContent)
            }
        }

        @Test
        fun `pdf`() {
            documentService.validateFile("test.pdf", testFileContent)
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `empty file`() {
            documentService.validateFile("empty.pdf", ByteArray(0))
        }

        @Test
        fun `large file`() {
            val largeContent = ByteArray(50_000_000)
            documentService.validateFile("large.pdf", largeContent)
        }

        @Test
        fun `only extension`() {
            documentService.validateFile(".pdf", testFileContent)
        }

        @Test
        fun `multiple extensions`() {
            documentService.validateFile("archive.tar.pdf", testFileContent)
        }

        @Test
        fun `mixed case`() {
            documentService.validateFile("document.PdF", testFileContent)
        }
    }
}

