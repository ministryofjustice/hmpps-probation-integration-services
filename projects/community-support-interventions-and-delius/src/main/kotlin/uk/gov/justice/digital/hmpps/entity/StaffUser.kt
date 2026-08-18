package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "user_")
class StaffUser(
    @Id
    @Column(name = "user_id")
    var id: Long,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @Column(name = "distinguished_name")
    var userName: String
)

interface StaffUserRepository : org.springframework.data.jpa.repository.JpaRepository<StaffUser, Long> {
    fun findByUserName(userName: String): StaffUser?

    fun findByStaffId(id: Long): StaffUser?
}