package uk.gov.justice.digital.hmpps.integrations.delius.offender

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Immutable
@Entity
class Offender(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")val crn: String,
    @Column(columnDefinition = "char(7)") val nomsNumber: String?,
)
