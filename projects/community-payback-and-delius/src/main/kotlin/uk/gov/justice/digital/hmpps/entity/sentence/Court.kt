package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Court(
    @Id
    @Column(name = "court_id")
    val id: Long,

    val code: String,

    val courtName: String,

    )