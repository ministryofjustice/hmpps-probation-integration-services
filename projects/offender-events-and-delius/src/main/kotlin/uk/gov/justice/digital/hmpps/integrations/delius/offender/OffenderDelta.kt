package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph

@NamedEntityGraph(
    name = "OffenderDelta.withOffender",
    attributeNodes = [NamedAttributeNode("offender")]
)
@Entity
class OffenderDelta(
    @Id @Column(name = "offender_delta_id")
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
