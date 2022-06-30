package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.Hibernate
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
data class CaseNote(
    @Id
    @Column(name = "contact_id", updatable = false)
    @SequenceGenerator(name = "idGenerator", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    val id: Long? = null,

    @Column(updatable = false)
    val offenderId: Long,

    @Column(name = "nomis_case_note_id", updatable = false)
    val nomisId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: CaseNoteType,

    @Lob
    val notes: String,

    @Column(name = "contact_date")
    val date: ZonedDateTime,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @Column(updatable = false)
    val staffId: Long,

    @Column(updatable = false)
    val staffEmployeeId: Long,

    @Column(updatable = false)
    val teamId: Long,

    @Column(updatable = false)
    val probationAreaId: Long,

    @Column(name = "sensitive")
    @Type(type = "yes_no")
    val isSensitive: Boolean = type.isSensitive,

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime", updatable = false)
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @Column(updatable = false)
    val trustProviderTeamId: Long = teamId,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val trustProviderFlag: Boolean = false,

    @Column(updatable = false)
    val partitionAreaId: Long = 0L,

    @Column(updatable = false, columnDefinition = "NUMBER")
    var softDeleted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as CaseNote

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(id = $id , offenderId = $offenderId , nomisId = $nomisId , type = $type , " +
            "notes = $notes , date = $date , startTime = $startTime , lastModifiedDate = $lastModifiedDateTime , " +
            "lastModifiedUserId = $lastModifiedUserId , createdByUserId = $createdByUserId , " +
            "createdDateTime = $createdDateTime , version = $version )"
    }
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class CaseNoteType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String,

    @Column(name = "sensitive_contact")
    @Type(type = "yes_no")
    val isSensitive: Boolean,
)

@Immutable
@Entity(name = "r_contact_type_nomis_type")
class CaseNoteNomisType(
    @Id
    @Column(name = "nomis_contact_type")
    val nomisCode: String,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: CaseNoteType,
) {
    companion object {
        const val DEFAULT_CODE = "NOMISD"
    }
}
