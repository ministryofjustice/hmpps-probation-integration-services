package uk.gov.justice.digital.hmpps.integrations.delius.compliance

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
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

    @Column(name = "event_id")
    val eventId: Long?,

    @Column(name = "actual_start_date")
    val actualStartDate: LocalDate?,

    @Column(name = "expected_start_date")
    val expectedStartDate: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    val nsiStatus: NsiStatus? = null,

    @Id
    @Column(name = "nsi_id")
    val id: Long = 0,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
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

    @Id
    @Column(name = "nsi_type_id") val id: Long
) {
    enum class Code(val value: String) {
        OPD_COMMUNITY_PATHWAY("OPD1")
    }
}

interface NsiRepository : JpaRepository<Nsi, Long> {

    fun findByPersonIdAndTypeCode(personId: Long, typeCode: String): List<Nsi>
}

fun NsiRepository.getAllBreaches(personId: Long): List<Nsi> = findByPersonIdAndTypeCode(personId, "BRE")

