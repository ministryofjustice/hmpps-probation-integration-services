package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
data class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(name = "nomsNumber", columnDefinition = "char(7)")
    val prisonerId: String,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)
