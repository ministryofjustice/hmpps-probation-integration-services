package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    val forename: String,
    val surname: String,
    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,
    @Id
    @Column(name = "staff_id")
    val id: Long,
) {
    companion object {
        const val INTENDED_STAFF_CODE = Team.INTENDED_TEAM_CODE + "U"
    }
}

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @Column(name = "distinguished_name")
    val username: String,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,
    @Id
    @Column(name = "user_id")
    val id: Long,
) {
    @Transient
    var email: String? = null

    @Transient
    var telephone: String? = null
}

interface StaffRepository : JpaRepository<Staff, Long> {
    @EntityGraph(attributePaths = ["user"])
    fun findByCode(code: String): Staff?
}

fun StaffRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("Staff", "code", code)
