package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Court(
    @Id
    @Column(name = "court_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Column(name = "court_name")
    val courtName: String,

    )