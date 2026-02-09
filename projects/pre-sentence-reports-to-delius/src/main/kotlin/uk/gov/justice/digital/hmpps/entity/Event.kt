package uk.gov.justice.digital.hmpps.entity

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

    val eventNumber: String
)