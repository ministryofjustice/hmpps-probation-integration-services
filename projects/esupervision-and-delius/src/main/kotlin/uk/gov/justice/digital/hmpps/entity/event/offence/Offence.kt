package uk.gov.justice.digital.hmpps.entity.event.offence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Column
    val description: String
)
