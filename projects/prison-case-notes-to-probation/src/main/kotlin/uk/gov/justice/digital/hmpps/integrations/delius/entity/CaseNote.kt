package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.Hibernate
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.converters.BooleanYesNoConverter
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Version

@Entity(name = "contact")
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
    @Convert(converter = BooleanYesNoConverter::class)
    val isSensitive: Boolean = type.isSensitive,

    @Column(name = "last_updated_user_id")
    val lastModifiedUserId: Long,

    @Column(name = "created_by_user_id", updatable = false)
    val createdByUserId: Long,

    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_datetime")
    val lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column(updatable = false)
    val trustProviderTeamId: Long = teamId,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val trustProviderFlag: Boolean = false,

    @Column(updatable = false)
    val partitionAreaId: Long = 0L,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
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
@Entity(name = "r_contact_type")
class CaseNoteType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String,

    @Column(name = "sensitive_contact")
    @Convert(converter = BooleanYesNoConverter::class)
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
)
