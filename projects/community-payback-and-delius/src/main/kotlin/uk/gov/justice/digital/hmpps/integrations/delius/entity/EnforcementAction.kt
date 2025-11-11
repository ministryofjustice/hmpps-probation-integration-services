package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Table(name = "r_enforcement_action")
@Immutable
class EnforcementAction(
    @Id
    @Column(name = "enforcement_action_id")
    val id: Long,

    val code: String,

    val description: String,

    val responseByPeriod: Long,

    @Convert(converter = YesNoConverter::class)
    val outstandingContactAction: Boolean
)