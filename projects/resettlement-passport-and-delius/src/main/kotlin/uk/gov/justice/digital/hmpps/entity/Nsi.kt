package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "nsi")
@Where(clause = "active_flag = 1 and soft_deleted = 0")
class Nsi(
    @Id
    @Column(name = "nsi_id", nullable = false)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id", nullable = false)
    val type: NsiType,

    @ManyToOne
    @JoinColumn(name = "nsi_sub_type_id", nullable = false)
    val subType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    val status: NsiStatus,

    @Column
    val referralDate: LocalDate,

    @Column
    val actualStartDate: ZonedDateTime? = null,

    @Lob
    @Column
    val notes: String? = null,

    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "active_flag", columnDefinition = "number")
    var active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Table(name = "r_nsi_type")
class NsiType(
    @Id
    @Column(name = "nsi_type_id")
    val id: Long = 0,

    @Column(name = "code")
    val code: String
)

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
interface NsiRepository : JpaRepository<Nsi, Long> {

    @Query(
        """
        select nsi from Nsi nsi
        join fetch nsi.status
        join fetch nsi.type
        where nsi.person.crn = :crn
        and nsi.type.code = 'DTR'
        and nsi.status.code = 'INIT'
        order by nsi.createdDatetime desc
    """
    )
    fun findDutyToReferByCrn(crn: String, page: PageRequest = PageRequest.of(0, 1)): Nsi?

    @Query(
        """
        select nsi from Nsi nsi
        join fetch nsi.status
        join fetch nsi.type
        where nsi.person.noms = :noms
        and nsi.type.code = 'DTR'
        and nsi.status.code = 'INIT'
        order by nsi.createdDatetime desc
    """
    )
    fun findDutyToReferByNoms(noms: String, page: PageRequest = PageRequest.of(0, 1)): Nsi?
}
