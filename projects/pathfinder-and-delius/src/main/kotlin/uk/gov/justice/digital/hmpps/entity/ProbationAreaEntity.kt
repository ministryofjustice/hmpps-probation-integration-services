package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@Table(name = "probation_area")
class ProbationAreaEntity(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    val description: String,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @OneToMany(mappedBy = "probationAreaId")
    val boroughs: List<Borough> = listOf()
)

@Immutable
@Entity
@Table(name = "district")
class District(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column(name = "code")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Id
    @Column(name = "borough_id")
    val id: Long,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Column(name = "code")
    val code: String,

    @OneToMany(mappedBy = "borough")
    val districts: List<District> = listOf()

)

interface ProbationAreaRepository : JpaRepository<ProbationAreaEntity, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.entity.ProbationAreaDistrict(pa.code, pa.description, d.code, d.description)
        from ProbationAreaEntity pa 
        join Borough b on b.probationAreaId = pa.id
        join District d on d.borough.id = b.id
        where pa.description not like 'ZZ%'
        and d.code <> '-1'
        and pa.selectable = true
        and d.selectable = true
        and b.selectable = true
    """
    )
    fun probationAreaDistricts(): List<ProbationAreaDistrict>
}

data class ProbationAreaDistrict(val pCode: String, val pDesc: String, val dCode: String, val dDesc: String)
