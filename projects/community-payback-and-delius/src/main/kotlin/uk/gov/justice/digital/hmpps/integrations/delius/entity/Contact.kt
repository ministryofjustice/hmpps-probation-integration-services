package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Table(name = "contact")
@Immutable
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val contactOutcome: ContactOutcome?,

    @ManyToOne
    @JoinColumn(name = "latest_enforcement_action_id")
    val latestEnforcementAction: EnforcementAction?,

    @Lob
    val notes: String?,

    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean?,

    @Convert(converter = YesNoConverter::class)
    val alertActive: Boolean?,

    @Version
    val rowVersion: Long
)