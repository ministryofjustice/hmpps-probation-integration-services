package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "event")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    val offenderId: Long,

    val eventNumber: Long
)