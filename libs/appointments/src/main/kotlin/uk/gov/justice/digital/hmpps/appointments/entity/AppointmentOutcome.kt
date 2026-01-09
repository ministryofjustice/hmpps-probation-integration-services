package uk.gov.justice.digital.hmpps.appointments.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@Table(name = "r_contact_outcome_type")
open class AppointmentOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    @Column(name = "code")
    override val code: String,

    @Column(name = "description")
    val description: String,

    @Column(name = "outcome_attendance")
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean?,

    @Column(name = "outcome_compliant_acceptable")
    @Convert(converter = YesNoConverter::class)
    val complied: Boolean?,

    @Convert(converter = YesNoConverter::class)
    val enforceable: Boolean?,
) : AppointmentEntities.CodedReferenceData