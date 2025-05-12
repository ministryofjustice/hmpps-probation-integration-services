package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Borough(
    @Id
    @Column(name = "borough_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String,
)
