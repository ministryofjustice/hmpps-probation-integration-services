package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Table(name = "r_offence")
data class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column
    val subCategoryDescription: String
)
