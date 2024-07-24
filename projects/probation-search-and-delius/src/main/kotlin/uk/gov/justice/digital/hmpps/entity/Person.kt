package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Table(name = "offender")
@Entity
class Person(
    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Id
    @Column(name = "offender_id")
    val id: Long
)