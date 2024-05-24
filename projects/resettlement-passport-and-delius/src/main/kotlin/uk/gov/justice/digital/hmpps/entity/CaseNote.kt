package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity(name = "ContactEntity")
@Table(name = "contact")
class CaseNote(
    @Id
    @Column(name = "contact_id", updatable = false)
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: CaseNoteType,

    @Lob
    val notes: String,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @Column(name = "staff_id")
    val staffId: Long,

    @Column(name = "team_id")
    val teamId: Long,

    @Column(updatable = false)
    val probationAreaId: Long,

    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    val lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column(updatable = false)
    val partitionAreaId: Long = 0L,

    @Column(name = "event_id")
    val eventId: Long? = null,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false
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
)

interface CaseNoteTypeRepository : JpaRepository<CaseNoteType, Long> {

    fun findByCode(code: String): CaseNoteType?
}

fun CaseNoteTypeRepository.getCode(code: String) =
    findByCode(code) ?: throw NotFoundException("CaseNoteType", "code", code)

