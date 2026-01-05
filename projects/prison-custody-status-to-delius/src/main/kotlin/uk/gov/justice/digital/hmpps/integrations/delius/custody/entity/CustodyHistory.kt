package uk.gov.justice.digital.hmpps.integrations.delius.custody.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime

@Entity
@Table(name = "custody_history")
class CustodyHistory(
    @Id
    @SequenceGenerator(
        name = "custody_history_id_generator",
        sequenceName = "custody_history_id_seq",
        allocationSize = 1
    )
    @GeneratedId(generator = "custody_history_id_generator")
    @Column(name = "custody_history_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(name = "historical_date", nullable = false)
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "custody_event_type_id", nullable = false)
    val type: ReferenceData,

    @Column
    val detail: String?,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "custody_id", nullable = false)
    val custody: Custody,

    @Column(nullable = false)
    val partitionAreaId: Long = 0
)

interface CustodyHistoryRepository : JpaRepository<CustodyHistory, Long>
