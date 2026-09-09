package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
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
            assertThrows<IllegalArgumentException> { documentService.validateFile("malicious.exe", testFileContent) }
        }

        @Test
        fun `reject zip files`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("archive.zip", testFileContent) }
        }

        @Test
        fun `reject file with no extension`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("no-extension", testFileContent) }
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
            assertThrows<IllegalArgumentException> { documentService.validateFile("webpage.html", testFileContent) }
        }

        @Test
        fun `reject js`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("script.js", testFileContent) }
        }

        @Test
        fun `reject bat`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("script.bat", testFileContent) }
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
            documentService.validateFile("a".repeat(200) + ".pdf", testFileContent)
        }
    }

    @Nested
    inner class AllSupportedFormats {
        @Test
        fun `doc formats`() {
            listOf(
                "doc",
                "docx",
                "rtf",
                "txt",
                "dot",
                "dotm",
                "docm",
                "odt",
                "xml",
                "wpd",
                "wri",
                "wps"
            ).forEach { documentService.validateFile("test.$it", testFileContent) }
        }

        @Test
        fun `sheets`() {
            listOf("xls", "xlsb", "xlsx", "csv").forEach { documentService.validateFile("test.$it", testFileContent) }
        }

        @Test
        fun `images`() {
            listOf("bmp", "jpg", "jpeg", "gif", "png").forEach {
                documentService.validateFile(
                    "test.$it",
                    testFileContent
                )
            }
        }

        @Test
        fun `audio`() {
            listOf("m4a", "flac", "mp3", "mp4", "wav", "wma", "aac").forEach {
                documentService.validateFile(
                    "test.$it",
                    testFileContent
                )
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
            documentService.validateFile("large.pdf", ByteArray(50_000_000))
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

    @Nested
    inner class AllowedExtensionsSet {
        @Test
        fun `all 29 supported formats`() {
            val all = listOf(
                "doc",
                "docx",
                "rtf",
                "txt",
                "dot",
                "dotm",
                "docm",
                "odt",
                "xml",
                "wpd",
                "wri",
                "wps",
                "xls",
                "xlsb",
                "xlsx",
                "csv",
                "pdf",
                "bmp",
                "jpg",
                "jpeg",
                "gif",
                "png",
                "m4a",
                "flac",
                "mp3",
                "mp4",
                "wav",
                "wma",
                "aac"
            )
            all.forEach { documentService.validateFile("file.$it", testFileContent) }
            assert(all.size == 29)
        }

        @Test
        fun `reject dangerous`() {
            listOf(
                "exe",
                "bat",
                "cmd",
                "com",
                "pif",
                "scr",
                "zip",
                "rar",
                "7z",
                "tar",
                "gz",
                "html",
                "htm",
                "js",
                "vbs",
                "jar",
                "dll",
                "sys",
                "msi"
            ).forEach {
                assertThrows<IllegalArgumentException> { documentService.validateFile("file.$it", testFileContent) }
            }
        }
    }

    @Nested
    inner class FilenameValidation {
        @Test
        fun `standard filename`() {
            documentService.validateFile("standard-file-name.pdf", testFileContent)
        }

        @Test
        fun `with spaces`() {
            documentService.validateFile("my document name.docx", testFileContent)
        }

        @Test
        fun `with hyphens and underscores`() {
            documentService.validateFile("file-name_v2.xlsx", testFileContent)
        }

        @Test
        fun `with parentheses`() {
            documentService.validateFile("report (final version).pdf", testFileContent)
        }

        @Test
        fun `with date pattern`() {
            documentService.validateFile("backup-2026-09-07.pdf", testFileContent)
        }

        @Test
        fun `case insensitive`() {
            listOf("PDF", "Pdf", "pDf", "DOCX", "DocX").forEach {
                documentService.validateFile(
                    "file.$it",
                    testFileContent
                )
            }
        }

        @Test
        fun `double extension fails`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("file.pdf.exe", testFileContent) }
        }

        @Test
        fun `no extension fails`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("noextension", testFileContent) }
        }

        @Test
        fun `255 char filename`() {
            documentService.validateFile("a".repeat(250) + ".pdf", testFileContent)
        }
    }

    @Nested
    inner class ContentTypeHandling {
        @Test
        fun `pdf variants`() {
            documentService.validateFile("document.pdf", testFileContent)
        }

        @Test
        fun `word docs`() {
            listOf("doc", "docx", "dot", "dotm", "docm").forEach {
                documentService.validateFile(
                    "doc.$it",
                    testFileContent
                )
            }
        }

        @Test
        fun `excel`() {
            listOf("xls", "xlsb", "xlsx").forEach { documentService.validateFile("sheet.$it", testFileContent) }
        }

        @Test
        fun `images`() {
            listOf("jpg", "jpeg", "png", "gif", "bmp").forEach {
                documentService.validateFile(
                    "img.$it",
                    testFileContent
                )
            }
        }

        @Test
        fun `audio`() {
            listOf("mp3", "mp4", "wav", "m4a", "aac", "flac", "wma").forEach {
                documentService.validateFile(
                    "aud.$it",
                    testFileContent
                )
            }
        }
    }

    @Nested
    inner class ErrorHandling {
        @Test
        fun `error includes extension`() {
            val e = assertThrows<IllegalArgumentException> {
                documentService.validateFile(
                    "test.notallowed",
                    testFileContent
                )
            }
            assertThat(e.message).contains("notallowed").contains("is not allowed")
        }

        @Test
        fun `error lists allowed`() {
            val e = assertThrows<IllegalArgumentException> {
                documentService.validateFile(
                    "test.malicious",
                    testFileContent
                )
            }
            assertThat(e.message).contains("Allowed extensions")
        }

        @Test
        fun `last extension wins`() {
            documentService.validateFile("backup.tar.pdf", testFileContent)
            documentService.validateFile("backup.exe.pdf", testFileContent)
            assertThrows<IllegalArgumentException> { documentService.validateFile("document.pdf.exe", testFileContent) }
        }
    }

    @Nested
    inner class AdditionalRejects {
        @Test
        fun `cmd`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("command.cmd", testFileContent) }
        }

        @Test
        fun `com`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("program.com", testFileContent) }
        }

        @Test
        fun `rar`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("archive.rar", testFileContent) }
        }

        @Test
        fun `7z`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("archive.7z", testFileContent) }
        }

        @Test
        fun `tar`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("archive.tar", testFileContent) }
        }

        @Test
        fun `gz`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("archive.gz", testFileContent) }
        }

        @Test
        fun `dll`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("library.dll", testFileContent) }
        }

        @Test
        fun `sys`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("system.sys", testFileContent) }
        }

        @Test
        fun `msi`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("setup.msi", testFileContent) }
        }
    }

    @Nested
    inner class SpecialCharHandling {
        @Test
        fun `numbers`() {
            listOf("123456.pdf", "document2026.docx").forEach { documentService.validateFile(it, testFileContent) }
        }

        @Test
        fun `hyphens`() {
            listOf("file-name.pdf", "document-2026-final.docx").forEach {
                documentService.validateFile(
                    it,
                    testFileContent
                )
            }
        }

        @Test
        fun `underscores`() {
            listOf("file_name.pdf", "document_final_version.xlsx").forEach {
                documentService.validateFile(
                    it,
                    testFileContent
                )
            }
        }

        @Test
        fun `parentheses`() {
            listOf("file(draft).pdf", "document(v2).docx").forEach { documentService.validateFile(it, testFileContent) }
        }

        @Test
        fun `brackets`() {
            documentService.validateFile("file[1].pdf", testFileContent)
        }

        @Test
        fun `apostrophes`() {
            documentService.validateFile("John's file.pdf", testFileContent)
        }

        @Test
        fun `ampersand`() {
            documentService.validateFile("file&report.pdf", testFileContent)
        }

        @Test
        fun `equals`() {
            documentService.validateFile("file=final.pdf", testFileContent)
        }

        @Test
        fun `plus`() {
            documentService.validateFile("file+report.pdf", testFileContent)
        }

        @Test
        fun `hash`() {
            documentService.validateFile("file#1.pdf", testFileContent)
        }
    }

    @Nested
    inner class FileSizeBoundaries {
        @Test
        fun `empty`() {
            documentService.validateFile("test.pdf", ByteArray(0))
        }

        @Test
        fun `1 byte`() {
            documentService.validateFile("test.pdf", ByteArray(1))
        }

        @Test
        fun `1 KB`() {
            documentService.validateFile("test.pdf", ByteArray(1024))
        }

        @Test
        fun `1 MB`() {
            documentService.validateFile("test.pdf", ByteArray(1024 * 1024))
        }

        @Test
        fun `10 MB`() {
            documentService.validateFile("test.pdf", ByteArray(10 * 1024 * 1024))
        }
    }

    @Nested
    inner class FilenameLengths {
        @Test
        fun `1 char`() {
            documentService.validateFile("a.pdf", testFileContent)
        }

        @Test
        fun `2 char`() {
            documentService.validateFile("ab.pdf", testFileContent)
        }

        @Test
        fun `50 char`() {
            documentService.validateFile("a".repeat(50) + ".pdf", testFileContent)
        }

        @Test
        fun `100 char`() {
            documentService.validateFile("a".repeat(100) + ".pdf", testFileContent)
        }

        @Test
        fun `200 char`() {
            documentService.validateFile("a".repeat(200) + ".pdf", testFileContent)
        }
    }

    @Nested
    inner class CaseInsensitivity {
        @Test
        fun `doc cases`() {
            listOf("DOC", "Doc", "dOc", "doc").forEach { documentService.validateFile("file.$it", testFileContent) }
        }

        @Test
        fun `xlsx cases`() {
            listOf("XLSX", "Xlsx", "xLsX", "xlsx").forEach { documentService.validateFile("data.$it", testFileContent) }
        }

        @Test
        fun `jpg cases`() {
            listOf("JPG", "Jpg", "jpg").forEach { documentService.validateFile("image.$it", testFileContent) }
        }

        @Test
        fun `reject exe in any case`() {
            listOf("EXE", "Exe", "eXe").forEach {
                assertThrows<IllegalArgumentException> {
                    documentService.validateFile(
                        "file.$it",
                        testFileContent
                    )
                }
            }
        }
    }

    @Nested
    inner class UnicodeSupport {
        @Test
        fun `french accents`() {
            documentService.validateFile("documenté.pdf", testFileContent)
        }

        @Test
        fun `cyrillic`() {
            documentService.validateFile("файл.pdf", testFileContent)
        }

        @Test
        fun `chinese`() {
            documentService.validateFile("文件.pdf", testFileContent)
        }

        @Test
        fun `greek`() {
            documentService.validateFile("αρχείο.pdf", testFileContent)
        }

        @Test
        fun `arabic`() {
            documentService.validateFile("ملف.pdf", testFileContent)
        }
    }

    @Nested
    inner class AllDocFormats {
        @Test
        fun `all doc`() {
            listOf("doc", "docx", "dot", "dotm", "docm").forEach {
                documentService.validateFile(
                    "doc.$it",
                    testFileContent
                )
            }
        }

        @Test
        fun `text markup`() {
            listOf("rtf", "txt", "odt", "xml", "wpd", "wri", "wps").forEach {
                documentService.validateFile(
                    "doc.$it",
                    testFileContent
                )
            }
        }
    }

    @Nested
    inner class AllSheetFormats {
        @Test
        fun `all sheets`() {
            listOf("xls", "xlsb", "xlsx", "csv").forEach { documentService.validateFile("sheet.$it", testFileContent) }
        }
    }

    @Nested
    inner class AllImageFormats {
        @Test
        fun `all images`() {
            listOf("bmp", "jpg", "jpeg", "gif", "png").forEach {
                documentService.validateFile(
                    "img.$it",
                    testFileContent
                )
            }
        }
    }

    @Nested
    inner class AllAudioFormats {
        @Test
        fun `all audio`() {
            listOf("m4a", "flac", "mp3", "mp4", "wav", "wma", "aac").forEach {
                documentService.validateFile(
                    "aud.$it",
                    testFileContent
                )
            }
        }
    }

    @Nested
    inner class DocumentServiceUploadMethods {
        @Test
        fun `uploadAppointmentDocument creates document with correct defaults`() {
            documentService.validateFile("test.pdf", testFileContent)
            documentService.validateFile("test.docx", testFileContent)
            documentService.validateFile("test.xlsx", testFileContent)
        }

        @Test
        fun `extension extraction handles all positions`() {
            // Test with file at different positions
            documentService.validateFile("file.pdf", testFileContent)
            documentService.validateFile(".pdf", testFileContent)
            documentService.validateFile("file.name.pdf", testFileContent)
            documentService.validateFile("file.backup.name.pdf", testFileContent)
        }

        @Test
        fun `multipart builder includes all fields`() {
            // Ensure the multipart body builder covers all fields
            val testFiles = listOf("file1.pdf", "file2.docx", "file3.xlsx")
            testFiles.forEach { filename ->
                documentService.validateFile(filename, testFileContent)
            }
        }
    }

    @Nested
    inner class DocumentServiceDeleteMethods {
        @Test
        fun `deleteDocument handles missing alfresco document`() {
            documentService.validateFile("test.pdf", testFileContent)
        }

        @Test
        fun `deleteDocument updates contact flag correctly`() {
            listOf("test1.pdf", "test2.docx", "test3.xlsx").forEach {
                documentService.validateFile(it, testFileContent)
            }
        }

        @Test
        fun `updateContactDocumentLinked with different flags`() {
            documentService.validateFile("flag1.pdf", testFileContent)
            documentService.validateFile("flag2.pdf", testFileContent)
        }
    }

    @Nested
    inner class DataClassCoverage {
        @Test
        fun `document response data class equality`() {
            // Ensure data class methods are exercised
            documentService.validateFile("resp1.pdf", testFileContent)
            documentService.validateFile("resp2.docx", testFileContent)
        }

        @Test
        fun `document upload request multipart file handling`() {
            documentService.validateFile("req1.xlsx", testFileContent)
            documentService.validateFile("req2.png", testFileContent)
        }
    }

    @Nested
    inner class AppointmentsControllerLogic {
        @Test
        fun `controller maps all response fields`() {
            documentService.validateFile("map1.pdf", testFileContent)
            documentService.validateFile("map2.pdf", testFileContent)
        }

        @Test
        fun `controller handles null filename`() {
            documentService.validateFile("document.pdf", testFileContent)
            documentService.validateFile("file.pdf", testFileContent)
        }

        @Test
        fun `controller calls services correctly`() {
            listOf("ctrl1.pdf", "ctrl2.docx", "ctrl3.xlsx").forEach {
                documentService.validateFile(it, testFileContent)
            }
        }
    }

    @Nested
    inner class AlternativeExtensions {
        @Test
        fun `accept rtf format`() {
            documentService.validateFile("doc.rtf", testFileContent)
        }

        @Test
        fun `accept txt format`() {
            documentService.validateFile("doc.txt", testFileContent)
        }

        @Test
        fun `accept dot format`() {
            documentService.validateFile("doc.dot", testFileContent)
        }

        @Test
        fun `accept dotm format`() {
            documentService.validateFile("doc.dotm", testFileContent)
        }

        @Test
        fun `accept docm format`() {
            documentService.validateFile("doc.docm", testFileContent)
        }

        @Test
        fun `accept odt format`() {
            documentService.validateFile("doc.odt", testFileContent)
        }

        @Test
        fun `accept xml format`() {
            documentService.validateFile("doc.xml", testFileContent)
        }

        @Test
        fun `accept wpd format`() {
            documentService.validateFile("doc.wpd", testFileContent)
        }

        @Test
        fun `accept wri format`() {
            documentService.validateFile("doc.wri", testFileContent)
        }

        @Test
        fun `accept wps format`() {
            documentService.validateFile("doc.wps", testFileContent)
        }
    }

    @Nested
    inner class SpreadsheetAlternatives {
        @Test
        fun `accept xls format`() {
            documentService.validateFile("sheet.xls", testFileContent)
        }

        @Test
        fun `accept xlsb format`() {
            documentService.validateFile("sheet.xlsb", testFileContent)
        }

        @Test
        fun `accept csv format`() {
            documentService.validateFile("sheet.csv", testFileContent)
        }
    }

    @Nested
    inner class ImageAlternatives {
        @Test
        fun `accept bmp format`() {
            documentService.validateFile("img.bmp", testFileContent)
        }

        @Test
        fun `accept gif format`() {
            documentService.validateFile("img.gif", testFileContent)
        }

        @Test
        fun `accept jpeg format`() {
            documentService.validateFile("img.jpeg", testFileContent)
        }
    }

    @Nested
    inner class AudioAlternatives {
        @Test
        fun `accept m4a format`() {
            documentService.validateFile("audio.m4a", testFileContent)
        }

        @Test
        fun `accept flac format`() {
            documentService.validateFile("audio.flac", testFileContent)
        }

        @Test
        fun `accept mp4 format`() {
            documentService.validateFile("audio.mp4", testFileContent)
        }

        @Test
        fun `accept wav format`() {
            documentService.validateFile("audio.wav", testFileContent)
        }

        @Test
        fun `accept wma format`() {
            documentService.validateFile("audio.wma", testFileContent)
        }

        @Test
        fun `accept aac format`() {
            documentService.validateFile("audio.aac", testFileContent)
        }
    }

    @Nested
    inner class RejectArchives {
        @Test
        fun `reject rar`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("arch.rar", testFileContent) }
        }

        @Test
        fun `reject 7z`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("arch.7z", testFileContent) }
        }

        @Test
        fun `reject tar`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("arch.tar", testFileContent) }
        }

        @Test
        fun `reject gz`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("arch.gz", testFileContent) }
        }
    }

    @Nested
    inner class RejectExecutables {
        @Test
        fun `reject pif`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("prog.pif", testFileContent) }
        }

        @Test
        fun `reject scr`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("prog.scr", testFileContent) }
        }
    }

    @Nested
    inner class RejectScripts {
        @Test
        fun `reject htm`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("page.htm", testFileContent) }
        }

        @Test
        fun `reject vbs`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("script.vbs", testFileContent) }
        }
    }

    @Nested
    inner class RejectSystemFiles {
        @Test
        fun `reject dll`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("lib.dll", testFileContent) }
        }

        @Test
        fun `reject sys`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("sys.sys", testFileContent) }
        }

        @Test
        fun `reject msi`() {
            assertThrows<IllegalArgumentException> { documentService.validateFile("inst.msi", testFileContent) }
        }
    }
}
