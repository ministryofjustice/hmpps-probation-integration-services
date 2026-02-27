package uk.gov.justice.digital.hmpps.entity.user

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "distinguished_name")
    val username: String,

    val forename: String,

    val surname: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,
)
