package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    val forename: String,
    val surname: String,
    @Column(name = "forename2")
    val middleName: String?,
    @OneToOne(mappedBy = "staff")
    val user: StaffUser?,
    @Id
    @Column(name = "staff_id")
    val id: Long,
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
    val id: Long,
)
