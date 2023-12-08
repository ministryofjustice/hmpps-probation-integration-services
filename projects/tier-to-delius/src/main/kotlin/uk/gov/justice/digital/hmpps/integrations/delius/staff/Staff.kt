package uk.gov.justice.digital.hmpps.integrations.delius.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long = 0,
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
)
