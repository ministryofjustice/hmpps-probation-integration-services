package uk.gov.justice.digital.hmpps.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "user_")
class AuditUser(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @Column(name = "distinguished_name")
    val username: String,
)
