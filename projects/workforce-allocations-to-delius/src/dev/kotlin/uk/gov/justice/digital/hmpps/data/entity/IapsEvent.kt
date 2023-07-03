package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "iaps_event")
class IapsEvent(
    @Id
    val eventId: Long,

    val iapsFlag: Long
)
