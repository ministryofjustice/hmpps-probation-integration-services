package uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDate

@Table
@Entity
@Immutable
@SQLRestriction("end_date is null or end_date > current_date")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,
    @Column
    val endDate: LocalDate? = null,
)

@Entity
@Immutable
@Table(name = "user_")
@SQLRestriction("end_date is null or end_date > current_date")
class StaffUser(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @Column(name = "distinguished_name")
    val username: String,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,
    @Column
    val endDate: LocalDate? = null,
)
