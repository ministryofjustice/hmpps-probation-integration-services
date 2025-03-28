package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.OfficeAddress
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    val description: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "end_date")
    var endDate: LocalDate? = null
)

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,
    val telephone: String?,
    val emailAddress: String?,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @ManyToMany
    @JoinTable(
        name = "team_office_location",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "office_location_id")]
    )
    @SQLRestriction("end_date is null or end_date > current_date")
    val addresses: List<OfficeLocation>,

    val startDate: LocalDate,
    val endDate: LocalDate?,

    @Id
    @Column(name = "team_id")
    val id: Long
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
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "borough_id")
    val id: Long,

    @ManyToMany
    @JoinTable(
        name = "r_level_2_head_of_level_2",
        joinColumns = [JoinColumn(name = "borough_id")],
        inverseJoinColumns = [JoinColumn(name = "staff_id")]
    )
    val pduHeads: List<Staff>,

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    val provider: Provider
)

interface BoroughRepository : JpaRepository<Borough, Long> {
    @Query(
        """
        select b from Borough b
        where b.code = :code
        and (b.provider.endDate is null or b.provider.endDate > current_date)
    """
    )
    fun findActiveByCode(code: String): Borough?
}

@Immutable
@Entity
@Table(name = "office_location")
class OfficeLocation(

    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,

    val description: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val townCity: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,

    @JoinColumn(name = "district_id")
    @ManyToOne
    val ldu: District,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

fun OfficeLocation.asAddress() = OfficeAddress(
    description,
    buildingName,
    buildingNumber,
    streetName,
    district,
    townCity,
    county,
    postcode,
    ldu.description,
    telephoneNumber,
    startDate,
    endDate
)