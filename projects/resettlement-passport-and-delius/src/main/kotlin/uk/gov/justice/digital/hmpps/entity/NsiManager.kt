package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class NsiManager(
    @Id
    @Column(name = "nsi_manager_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "nsi_id")
    val nsi: Nsi,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Table(name = "probation_area")
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Column(name = "description")
    val description: String,

    @OneToOne
    @JoinColumn(
        name = "institution_id",
        referencedColumnName = "institution_id",
        updatable = false
    )
    val institution: Institution? = null

)

@Entity
@Table(name = "team")
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Column(name = "description")
    val description: String
)

@Immutable
@Entity
@Table(name = "r_institution")
class Institution(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column(name = "nomis_cde_code")
    val nomisCode: String

)

interface TeamRepository : JpaRepository<Team, Long>

interface NsiManagerRepository : JpaRepository<NsiManager, Long> {

    @Query(
        """
        select nm from NsiManager nm
        join fetch nm.probationArea
        join fetch nm.team
        join fetch nm.staff
        where nm.nsi.id = :nsiId
    """
    )
    fun getNSIManagerByNsi(nsiId: Long): NsiManager?
}
