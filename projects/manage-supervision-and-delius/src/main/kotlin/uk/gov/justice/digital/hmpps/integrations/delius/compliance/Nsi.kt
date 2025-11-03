package uk.gov.justice.digital.hmpps.integrations.delius.compliance

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "nsi")
@SQLRestriction("soft_deleted = 0")
class Nsi(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id")
    val type: NsiType,

    @ManyToOne
    @JoinColumn(name = "nsi_sub_type_id")
    val subType: ReferenceData? = null,

    @Column(name = "event_id")
    val eventId: Long?,

    @Column(name = "rqmnt_id")
    val requirementId: Long? = null,

    @Column(name = "actual_start_date")
    val actualStartDate: LocalDate?,

    @Column(name = "expected_start_date")
    val expectedStartDate: LocalDate?,

    @Column(name = "referral_date")
    val referralDate: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    val nsiStatus: NsiStatus? = null,

    @Id
    @Column(name = "nsi_id")
    val id: Long = 0,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) {
    fun startDate(): LocalDate? = actualStartDate ?: expectedStartDate
}

@Entity
@Immutable
@Table(name = "r_nsi_status")
class NsiStatus(
    @Id
    @Column(name = "nsi_status_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String
)

@Immutable
@Entity
@Table(name = "r_nsi_type")
class NsiType(

    val code: String,

    val description: String,

    @Id
    @Column(name = "nsi_type_id") val id: Long
) {
    enum class Code(val value: String) {
        OPD_COMMUNITY_PATHWAY("OPD1")
    }
}

interface NsiRepository : JpaRepository<Nsi, Long> {
    @EntityGraph(attributePaths = ["type", "nsiStatus"])
    fun findByPersonIdAndTypeCode(personId: Long, typeCode: String): List<Nsi>
    fun countByPersonIdAndTypeCode(personId: Long, typeCode: String): Int

    fun findByPersonIdAndActiveIsTrue(personId: Long): List<Nsi>
}

fun NsiRepository.getAllBreaches(personId: Long): List<Nsi> = findByPersonIdAndTypeCode(personId, "BRE")
fun NsiRepository.countBreaches(personId: Long): Int = countByPersonIdAndTypeCode(personId, "BRE")

