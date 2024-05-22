package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String? = null,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,

    val probationAreaId: Long,

    @Id
    @Column(name = "staff_id")
    @SequenceGenerator(name = "staff_id_seq", sequenceName = "staff_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staff_id_seq")
    val id: Long = 0,
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Id
    @Column(name = "user_id")
    val id: Long
)

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
        probationAreaId: Long,
        forename: String,
        surname: String
    ): Staff?

    @Query(
        """
        select officer_code from staff
        where regexp_like(officer_code, ?1, 'i')
        order by officer_code desc
        fetch next 1 rows only
        """,
        nativeQuery = true
    )
    fun getLatestStaffReference(regex: String): String?
}

interface StaffTeamRepository : JpaRepository<StaffTeam, StaffTeamId>
