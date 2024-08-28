package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District?,

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val probationArea: ProbationArea,

    @Id
    @Column(name = "team_id")
    val id: Long
) {
    companion object {
        val POM_SUFFIX = "POM"
        val UNALLOCATED_SUFFIX = "ALL"
    }
}

interface TeamRepository : JpaRepository<Team, Long> {
    @EntityGraph(attributePaths = ["district"])
    fun findByCode(code: String): Team?
}

fun TeamRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("Team", "code", code)

@Immutable
@Entity
class District(

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "probation_area")
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    val description: String,

    @OneToOne
    @JoinColumn(
        name = "institution_id",
        referencedColumnName = "institution_id",
        updatable = false
    )
    val institution: Institution? = null
)

interface ProbationAreaRepository : JpaRepository<ProbationArea, Long> {
    fun findByInstitutionNomisCode(code: String): ProbationArea?
}

fun ProbationAreaRepository.getByNomisCdeCode(code: String) =
    findByInstitutionNomisCode(code) ?: throw NotFoundException("ProbationArea", "nomisCdeCode", code)

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
