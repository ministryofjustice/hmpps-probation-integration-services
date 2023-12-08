package uk.gov.justice.digital.hmpps.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime

@Immutable
@Entity
class BusinessInteraction(
    @Id
    @Column(name = "business_interaction_id", nullable = false)
    val id: Long,
    @Column(name = "business_interaction_code", nullable = false)
    val code: String,
    @Column(name = "enabled_date")
    val enabledDate: ZonedDateTime,
)
