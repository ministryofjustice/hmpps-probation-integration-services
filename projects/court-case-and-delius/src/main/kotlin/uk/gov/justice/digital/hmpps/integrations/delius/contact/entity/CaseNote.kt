package uk.gov.justice.digital.hmpps.integrations.delius.contact.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
class CaseNote(
    @Id
    @Column(name = "contact_id", updatable = false)
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,

    val externalReference: String,

    @Column(updatable = false)
    val offenderId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: CaseNoteType,

    @Lob
    var notes: String,

    @Column(name = "contact_date")
    var date: LocalDate,

    @Column(name = "contact_start_time")
    var startTime: ZonedDateTime,

    @Column(updatable = false)
    val staffId: Long,

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

    @Column(updatable = false, columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,

    @Column(updatable = false)
    val partitionAreaId: Long = 0L,

    @Column(name = "event_id")
    val eventId: Long? = null,

    @Column(updatable = false, columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class CaseNoteType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String,

    val description: String,

    @Column(name = "sensitive_contact")
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean
) {
    companion object {
        const val DEFAULT_CODE = "C294"
    }
}
