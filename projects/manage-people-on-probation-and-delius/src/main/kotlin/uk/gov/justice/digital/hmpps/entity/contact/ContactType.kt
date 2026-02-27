package uk.gov.justice.digital.hmpps.entity.contact

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.model.request.CreateAppointment

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String,

    val description: String,

    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendance: Boolean = false,

    @Column(name = "contact_outcome_flag")
    @Convert(converter = YesNoConverter::class)
    val outcomeRequired: Boolean? = false, // Y=required, N=optional, B=not allowed
) {
    fun isDeliusManaged() = CreateAppointment.Type.entries.none { it.code == code }
}
