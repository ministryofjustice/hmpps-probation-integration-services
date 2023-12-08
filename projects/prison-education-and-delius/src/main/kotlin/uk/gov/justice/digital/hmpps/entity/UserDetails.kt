package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.Immutable

@Entity
@Immutable
@Table(name = "user_")
data class UserDetails(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @Column(name = "distinguished_name")
    val username: String,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,
)
