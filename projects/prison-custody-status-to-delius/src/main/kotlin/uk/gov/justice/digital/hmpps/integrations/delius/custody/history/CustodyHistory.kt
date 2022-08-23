package uk.gov.justice.digital.hmpps.integrations.delius.custody.history

import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "custody_history")
class CustodyHistory(
    @Id
    @SequenceGenerator(name = "custody_history_id_generator", sequenceName = "custody_history_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "custody_history_id_generator")
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
    val partitionAreaId: Long = 0,
)
