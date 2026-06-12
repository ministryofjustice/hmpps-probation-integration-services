package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Table(name = "main_offence")
@Immutable
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,
)

@Entity
@Table(name = "r_offence")
@Immutable
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long = 0,

    @Column(name = "code", columnDefinition = "char(5)")
    val code: String,

    @Column(name = "description")
    val description: String,
)

@Entity
@Table(name = "additional_offence")
@Immutable
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,
)