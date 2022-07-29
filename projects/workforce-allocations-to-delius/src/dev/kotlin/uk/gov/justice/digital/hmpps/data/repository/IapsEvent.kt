package uk.gov.justice.digital.hmpps.data.repository

import org.hibernate.annotations.Immutable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "iaps_event")
class IapsEvent(
    @Id
    val eventId: Long,

    val iapsFlag: Long
)
