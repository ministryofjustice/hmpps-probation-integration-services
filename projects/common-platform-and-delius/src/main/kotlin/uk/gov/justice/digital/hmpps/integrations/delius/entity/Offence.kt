package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@SequenceGenerator(name = "main_offence_id_seq", sequenceName = "main_offence_id_seq", allocationSize = 1)
@Table(name = "main_offence")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    @GeneratedId(generator = "main_offence_id_seq")
    val id: Long? = null,

    @Column(name = "offence_date")
    val date: LocalDate,

    @Column(name = "offence_count")
    val count: Int,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column
    val tics: Long? = null,

    @Column
    val verdict: String? = null,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(nullable = false)
    val partitionAreaId: Long = 0,

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @ManyToOne
    @JoinColumn(name = "detailed_offence_id")
    val detailedOffence: DetailedOffence?,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
)

@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(5)")
    val code: String,

    @Column(name = "description")
    val description: String
)

@Entity
@Table(name = "r_detailed_offence")
@EntityListeners(AuditingEntityListener::class)
@Immutable
class DetailedOffence(
    @Id
    @Column(name = "detailed_offence_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @Column(name = "cja_code", columnDefinition = "varchar2(10)")
    var code: String, // Criminal Justice System code

    @Column(name = "offence_description")
    var description: String?, // Criminal Justice System title

    @Column
    var startDate: LocalDate,

    @Column
    var endDate: LocalDate?,

    @Column(name = "ho_code", columnDefinition = "varchar2(6)")
    var homeOfficeCode: String?,

    @Column(name = "ho_description")
    var homeOfficeDescription: String?,

    @Column
    var legislation: String?,

    @ManyToOne
    @JoinColumn(name = "court_category_id")
    var category: ReferenceData?,

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
)

@Entity
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "additional_offence_seq", sequenceName = "additional_offence_seq", allocationSize = 1)
class AdditionalOffence(

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: Offence,

    @Column(name = "offence_date")
    val date: LocalDateTime?,

    @ManyToOne
    @JoinColumn(name = "detailed_offence_id")
    val detailedOffence: DetailedOffence?,

    val offenceCount: Long? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @GeneratedId(generator = "additional_offence_seq")
    @Column(name = "additional_offence_id")
    val id: Long? = null
) {
    @CreatedBy
    var createdByUserId: Long = 0

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0

    @Column
    val partitionAreaId: Long = 0
}

interface MainOffenceRepository : JpaRepository<MainOffence, Long>

interface OffenceRepository : JpaRepository<Offence, Long> {
    fun findByCode(offenceCode: String): Offence?
}

fun OffenceRepository.findOffence(offenceCode: String) =
    findByCode(offenceCode) ?: throw NotFoundException("Offence", "offenceCode", offenceCode)

interface DetailedOffenceRepository : JpaRepository<DetailedOffence, Long> {
    fun findByCode(code: String): DetailedOffence?
}

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long>
