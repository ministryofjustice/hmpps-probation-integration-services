package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,
    @Column(name = "distinguished_name")
    val username: String,
)
