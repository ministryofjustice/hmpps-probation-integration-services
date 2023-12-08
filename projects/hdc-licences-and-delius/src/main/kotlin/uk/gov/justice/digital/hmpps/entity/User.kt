package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: StaffEntity? = null,
    @Column(name = "distinguished_name")
    val username: String,
)
