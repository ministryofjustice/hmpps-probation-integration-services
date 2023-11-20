package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import java.time.ZonedDateTime

@Entity
@Immutable
class Disposal(
    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody?,

    @Column(name = "disposal_date")
    val startDate: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val disposalType: ReferenceData,

    @Column(name = "notional_end_date")
    val endDate: ZonedDateTime? = null,

    @Column
    val terminationDate: ZonedDateTime? = null,

    @Column(name = "entry_length")
    val entryLength: Long? = null,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val entryLengthUnit: ReferenceData? = null,

    @Column(name = "length_in_days")
    val lengthInDays: Long? = null,

    @ManyToOne
    @JoinColumn(name = "disposal_termination_reason_id")
    val terminationReason: ReferenceData?,

    @Column(name = "upw", columnDefinition = "number")
    val upw: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

interface DisposalRepository : JpaRepository<Disposal, Long> {

    @Query(
        """ 
        select d from Disposal d where d.event.person.crn = :crn
        and d.softDeleted = false
    """
    )
    fun getByCrn(crn: String): List<Disposal>
}

@Entity
@Immutable
class Custody(
    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val status: ReferenceData,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

interface CustodyRepository : JpaRepository<Custody, Long>
