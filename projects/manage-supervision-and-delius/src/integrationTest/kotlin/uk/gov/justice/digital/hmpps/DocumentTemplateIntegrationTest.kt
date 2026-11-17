package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.sql.Date
import java.time.LocalDate

class DocumentTemplateIntegrationTest : IntegrationTestBase() {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val templateId = 987654321L
    private val templateName = "integration-template.docx"
    private val templateContent = "template bytes".toByteArray()

    @BeforeEach
    fun insertDocumentTemplate() {
        jdbcTemplate.update(
            "INSERT INTO document_template (document_template_id, template_name, date_created_date, template) VALUES (?, ?, ?, ?)",
            templateId,
            templateName,
            Date.valueOf(LocalDate.of(2025, 1, 2)),
            templateContent
        )
        jdbcTemplate.update(
            "INSERT INTO object_type_document_template (document_template_id, object_primary_key_id) VALUES (?, ?)",
            templateId,
            ContactGenerator.APPT_CT_1.id
        )
    }

    @AfterEach
    fun removeDocumentTemplate() {
        jdbcTemplate.update(
            "DELETE FROM object_type_document_template WHERE document_template_id = ?",
            templateId
        )
        jdbcTemplate.update("DELETE FROM document_template WHERE document_template_id = ?", templateId)
    }

    @Test
    fun `returns templates for a contact type`() {
        mockMvc.get("/contact/types/${ContactGenerator.APPT_CT_1.code}/templates") {
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].id") { value(templateId) }
            jsonPath("$[0].name") { value(templateName) }
            jsonPath("$[0].dateCreated") { value("2025-01-02") }
        }
    }

    @Test
    fun `downloads the document template as an attachment`() {
        val response = mockMvc.get(
            "/contact/types/${ContactGenerator.APPT_CT_1.code}/templates/$templateName/download"
        ) {
            withToken()
        }.andExpect {
            status { isOk() }
        }.andReturn().response

        assertEquals("application/octet-stream", response.contentType)
        assertEquals("attachment; filename=\"$templateName\"", response.getHeader("Content-Disposition"))
        assertArrayEquals(templateContent, response.contentAsByteArray)
    }

    @Test
    fun `does not download a template linked to another contact type`() {
        mockMvc.get("/contact/types/${ContactGenerator.APPT_CT_2.code}/templates/$templateName/download") {
            withToken()
        }.andExpect {
            status { isNotFound() }
        }
    }
}
