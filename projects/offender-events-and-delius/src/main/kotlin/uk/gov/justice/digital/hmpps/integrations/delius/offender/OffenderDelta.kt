package uk.gov.justice.digital.hmpps.integrations.delius.offender

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import java.time.ZonedDateTime

@NamedEntityGraph(
    name = "OffenderDelta.withOffender",
    attributeNodes = [NamedAttributeNode("offender")],
)
@Entity
class OffenderDelta(
    @Id
    @Column(name = "offender_delta_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    @NotFound(action = NotFoundAction.IGNORE)
    val offender: Offender?,
    val dateChanged: ZonedDateTime,
    val action: String,
    val sourceTable: String,
    val sourceRecordId: Long,
)
