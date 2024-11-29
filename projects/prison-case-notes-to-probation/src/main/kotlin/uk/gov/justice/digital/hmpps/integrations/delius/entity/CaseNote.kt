package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
data class CaseNote(
    @Id
    @Column(name = "contact_id", updatable = false)
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,

    @Column(updatable = false)
    val offenderId: Long,

    @Column(updatable = false)
    val eventId: Long?,

    @Column(updatable = false)
    val nsiId: Long?,

    @Column(name = "nomis_case_note_id", updatable = false)
    val nomisId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: CaseNoteType,

    @Column(updatable = false, columnDefinition = "varchar2(200)")
    val description: String?,

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
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean = type.isSensitive,

    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_datetime")
    val lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0,

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @Column(updatable = false)
    val trustProviderTeamId: Long = teamId,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,

    @Column(updatable = false)
    val partitionAreaId: Long = 0L,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as CaseNote

        return id == other.id
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
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean
) {
    companion object {
        const val DEFAULT_CODE = "NOMISD"
    }
}

@Immutable
@Entity(name = "r_contact_type_nomis_type")
class CaseNoteNomisType(
    @Id
    @Column(name = "nomis_contact_type")
    val nomisCode: String,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: CaseNoteType
)
