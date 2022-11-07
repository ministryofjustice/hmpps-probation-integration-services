package uk.gov.justice.digital.hmpps.integrations.delius.offender

import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.OneToOne

@NamedEntityGraph(
    name = "OffenderDelta.withOffender",
    attributeNodes = [NamedAttributeNode("offender")]
)
@Entity
class OffenderDelta(
    @Id @Column(name = "offender_delta_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "offender_id")
    val offender: Offender?,

    val dateChanged: ZonedDateTime,
    val action: String,
    val sourceTable: String,
    val sourceRecordId: Long,
)
