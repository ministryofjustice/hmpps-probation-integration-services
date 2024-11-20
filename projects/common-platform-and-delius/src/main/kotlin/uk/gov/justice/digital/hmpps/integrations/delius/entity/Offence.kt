package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import jakarta.persistence.GenerationType.SEQUENCE
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@SequenceGenerator(name = "main_offence_id_seq", sequenceName = "main_offence_id_seq", allocationSize = 1)
@Table(name = "main_offence")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "main_offence_id_seq")
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
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    @Column
    var lastUpdatedUserId: Long = 0,

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column
    val detailedOffenceId: Long? = null,
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
@SequenceGenerator(name = "detailed_offence_id_seq", sequenceName = "detailed_offence_id_seq", allocationSize = 1)
class DetailedOffence(
    @Id
    @Column(name = "detailed_offence_id")
    @GeneratedValue(strategy = SEQUENCE, generator = "detailed_offence_id_seq")
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

    @Column(columnDefinition = "varchar2(8)")
    var pnldCode: String? = null, // Police National Legal Database code

    @Column
    var legislation: String?,

    @ManyToOne
    @JoinColumn(name = "court_category_id")
    var category: ReferenceData?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_sexual_offence")
    var schedule15SexualOffence: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_violent_offence")
    var schedule15ViolentOffence: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "cja_2003_s327_4a")
    var cjaSection327Subsection4A: Boolean? = null, // Offences listed in CJA 2003 section 327, subsection 4A.

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime? = null,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime? = null
)

interface MainOffenceRepository : JpaRepository<MainOffence, Long>

interface OffenceRepository : JpaRepository<Offence, Long> {
    fun findByCode(offenceCode: String): Offence?
}

fun OffenceRepository.findOffence(offenceCode: String) =
    findByCode(offenceCode) ?: throw NotFoundException("Offence", "offenceCode", offenceCode)

interface DetailedOffenceRepository : JpaRepository<DetailedOffence, Long> {
    fun findByCode(code: String): DetailedOffence?
}
