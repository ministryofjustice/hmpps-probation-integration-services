package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Immutable
@Entity
class BusinessInteraction(
    @Id @Column(name = "business_interaction_id", nullable = false) val id: Long,

    @Column(name = "business_interaction_code", nullable = false)
    val code: String,

    @Column(name = "enabled_date")
    val enabledDate: ZonedDateTime,
)
