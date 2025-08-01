package uk.gov.justice.digital.hmpps.service.entity

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

    @Id
    @Column(name = "staff_id")
    val id: Long
) {
    fun isUnallocated() = code.endsWith("U")
}

@Immutable
@Entity
@Table(name = "user_")
class StaffUser(

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Id
    @Column(name = "user_id")
    val id: Long,
)

interface StaffRepository : JpaRepository<Staff, Long> {
    @Query("select s from StaffUser su join su.staff s where upper(su.username) = upper(:username)")
    fun findByUsername(username: String): Staff?
}