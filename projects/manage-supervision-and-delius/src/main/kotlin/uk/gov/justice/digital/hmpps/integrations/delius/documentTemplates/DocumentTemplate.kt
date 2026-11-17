package uk.gov.justice.digital.hmpps.integrations.delius.documentTemplates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "document_template")
class DocumentTemplate(
    @Id
    @Column(name = "document_template_id")
    val id: Long = 0,
    @Column(name = "template_name")
    val name: String,
    @Column(name = "date_created_date")
    val dateCreated: LocalDate,
    @Lob
    @Column(name = "template")
    val template: ByteArray,
)

interface DocumentTemplateProjection {
    val id: Long
    val name: String
    val dateCreated: LocalDateTime
}

interface DocumentTemplateRepository : JpaRepository<DocumentTemplate, Long> {

    @Query(
        nativeQuery = true,
        value = """
            SELECT dt.document_template_id AS id,
             dt.template_name AS name,
             dt.date_created_date AS dateCreated
            FROM document_template dt
            JOIN object_type_document_template dtct ON dt.document_template_id = dtct.document_template_id
            JOIN r_contact_type ct ON dtct.object_primary_key_id = ct.contact_type_id
            WHERE ct.code = :code"""
    )
    fun findAllByContactTypeCode(code: String): List<DocumentTemplateProjection>

    @Query(
        nativeQuery = true,
        value = """
            SELECT dt.*
            FROM document_template dt
            JOIN object_type_document_template dtct ON dt.document_template_id = dtct.document_template_id
            JOIN r_contact_type ct ON dtct.object_primary_key_id = ct.contact_type_id
            WHERE ct.code = :code
              AND dt.template_name = :name"""
    )
    fun findByContactTypeCodeAndName(code: String, name: String): DocumentTemplate?
}