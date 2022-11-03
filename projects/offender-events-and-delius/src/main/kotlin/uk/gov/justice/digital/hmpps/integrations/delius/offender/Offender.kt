package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Immutable
@Entity
class Offender(
    @Id @Column(name = "offender_id") val id: Long,
    @Column(columnDefinition = "char(7)")val crn: String,
    @Column(columnDefinition = "char(7)") val nomsNumber: String?
)
