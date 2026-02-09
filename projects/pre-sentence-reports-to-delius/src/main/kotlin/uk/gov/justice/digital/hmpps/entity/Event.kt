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

    /**
     * Stored as a String to match the DB column type; API/DTO exposes this as a numeric value.
     * Conversion between String and numeric forms is handled in the service/mapping layer.
     */
    val eventNumber: String
)