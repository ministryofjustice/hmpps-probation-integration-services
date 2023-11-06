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
    val selectable: Boolean,

    val description: String,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Column(columnDefinition = "char(1)")
    val establishment: String?,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @OneToMany(mappedBy = "probationArea")
    val boroughs: List<Borough> = listOf()
)

@Immutable
@Entity
@Table(name = "district")
class District(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

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
    val selectable: Boolean,

    @Id
    @Column(name = "borough_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationAreaEntity,

    @Column(name = "code")
    val code: String,

    @OneToMany(mappedBy = "borough")
    val districts: List<District> = listOf()

)

interface ProbationAreaRepository : JpaRepository<ProbationAreaEntity, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.entity.ProbationAreaDistrict(pa.code, pa.description, d.code, d.description)
        from District d
        join d.borough b
        join b.probationArea pa
        where pa.description not like 'ZZ%'
        and d.code <> '-1'
        and pa.selectable = true
        and d.selectable = true
        and b.selectable = true
        and (pa.establishment is null or pa.establishment <> 'Y')
    """
    )
    fun probationAreaDistricts(): List<ProbationAreaDistrict>

    @Query(
        """
        select new uk.gov.justice.digital.hmpps.entity.ProbationAreaDistrict(pa.code, pa.description, d.code, d.description)
        from District d
        join d.borough b
        join b.probationArea pa
        where pa.description not like 'ZZ%'
        and d.code <> '-1'
        and (pa.establishment is null or pa.establishment <> 'Y')
    """
    )
    fun probationAreaDistrictsNonSelectable(): List<ProbationAreaDistrict>
}

data class ProbationAreaDistrict(val pCode: String, val pDesc: String, val dCode: String, val dDesc: String)
