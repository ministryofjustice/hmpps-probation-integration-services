package uk.gov.justice.digital.hmpps.entity.contact

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String,

    @Column(name = "outcome_attendance")
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean? = null,

    @Column(name = "outcome_compliant_acceptable")
    @Convert(converter = YesNoConverter::class)
    val complied: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    val enforceable: Boolean? = null,
)
