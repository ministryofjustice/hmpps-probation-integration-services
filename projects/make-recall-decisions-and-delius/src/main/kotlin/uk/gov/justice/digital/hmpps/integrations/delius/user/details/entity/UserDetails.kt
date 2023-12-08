package uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "user_")
class UserDetails(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @Column(name = "distinguished_name")
    val username: String,
    @Column
    val forename: String,
    @Column(name = "forename2")
    val middleName: String?,
    @Column(name = "surname")
    val surname: String,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: UserDetailsStaff?,
)

@Entity
@Immutable
@Table(name = "staff")
@SQLRestriction("end_date is null or end_date > current_date")
class UserDetailsStaff(
    @Id
    @Column(name = "staff_id")
    val id: Long,
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    @OneToOne(mappedBy = "staff")
    val user: UserDetails?,
    @Column
    val endDate: LocalDate? = null,
)
