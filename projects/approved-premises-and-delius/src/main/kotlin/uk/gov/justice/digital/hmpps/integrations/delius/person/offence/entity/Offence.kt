package uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import java.time.LocalDate

interface CaseOffence {
    val code: String
    val description: String
    val date: LocalDate?
    val main: Boolean
    val eventNumber: String
}

@Immutable
@Table(name = "main_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,
    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: Offence,
    @Column(name = "offence_date")
    val date: LocalDate,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean,
    @Id
    @Column(name = "main_offence_id")
    val id: Long,
)

@Immutable
@Table(name = "additional_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
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
    val id: Long,
)

@Immutable
@Table(name = "r_offence")
@Entity
class Offence(
    @Column(columnDefinition = "char(5)")
    val code: String,
    @Column
    val description: String,
    @Id
    @Column(name = "offence_id")
    val id: Long,
)

interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    @Query(
        """
        select mo.offence.code as code, mo.offence.description as description, mo.date as date, true as main, mo.event.number as eventNumber
        from MainOffence mo
        where mo.event.personId = :personId
        union all
        select ao.offence.code, ao.offence.description, ao.date, false, ao.event.number
        from AdditionalOffence ao
        where ao.event.personId = :personId
    """,
    )
    fun findOffencesFor(personId: Long): List<CaseOffence>
}
