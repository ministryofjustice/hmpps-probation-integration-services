package uk.gov.justice.digital.hmpps.entity.document

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "document")
@SQLRestriction("table_name = 'CONTACT'")
class Document (
    @Id
    @Column(name = "document_id")
    val id: Long = 0,

    @Column(name ="primary_key_id")
    val primaryKeyId: Long? = null,

    @Column(name = "table_name")
    val tableName: String? = null,

)