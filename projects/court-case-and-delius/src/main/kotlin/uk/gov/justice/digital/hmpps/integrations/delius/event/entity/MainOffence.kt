package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "main_offence")
@Where(clause = "soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(name = "offence_date")
    val date: LocalDate,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)

interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    fun findByEvent(event: Event): MainOffence
}

@Immutable
@Table(name = "additional_offence")
@Entity
@Where(clause = "soft_deleted = 0")
class AdditionalOffence(

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: Offence,

    @Column(name = "offence_date")
    val date: LocalDate?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "additional_offence_id")
    val id: Long
)

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long> {
    fun findByEvent(event: Event): List<AdditionalOffence>
}

@Immutable
@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,
    val description: String
)
