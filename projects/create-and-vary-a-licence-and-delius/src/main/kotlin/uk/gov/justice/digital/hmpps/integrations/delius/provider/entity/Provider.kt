package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @Id
    @Column(name = "team_id")
    val id: Long
)

interface TeamRepository : JpaRepository<Team, Long>

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

interface BoroughRepository : JpaRepository<Borough?, Long?> {
    @Query(
        """
        select b from Borough b
        where b.code = :code
        and (b.provider.endDate is null or b.provider.endDate > current_date)
    """
    )
    fun findActiveByCode(code: String): Borough?
}
