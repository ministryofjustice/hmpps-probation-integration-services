package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@Table(name = "probation_area")
class ProbationAreaEntity(

    val description: String,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @OneToMany(mappedBy = "probationAreaId", fetch = FetchType.EAGER)
    val boroughs: List<Borough> = listOf(),
)

@Immutable
@Entity
@Table(name = "district")
class District(

    @Column(name = "code")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Id
    @Column(name = "district_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Id
    @Column(name = "borough_id")
    val boroughId: Long,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Column(name = "code")
    val code: String,

    @OneToMany(mappedBy = "borough")
    val districts: List<District> = listOf(),

)

interface ProbationAreaRepository : JpaRepository<ProbationAreaEntity, Long>{
    @Query("""
        select p from ProbationAreaEntity p
        join fetch p.boroughs b
    """)
    override fun findAll(): List<ProbationAreaEntity>
}