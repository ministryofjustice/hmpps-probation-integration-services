package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(
    @Column(columnDefinition = "char(7)")
    val crn: String,
    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,
    @Id
    @Column(name = "offender_id")
    val id: Long,
)
