package uk.gov.justice.digital.hmpps.integrations.delius.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @Id
    @Column(name = "user_id")
    val id: Long = 0,
    @Column(name = "distinguished_name")
    val username: String,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: StaffWithUser? = null,
)
