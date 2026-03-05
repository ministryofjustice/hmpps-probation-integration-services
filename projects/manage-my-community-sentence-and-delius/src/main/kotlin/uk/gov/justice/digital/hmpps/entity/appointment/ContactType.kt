package uk.gov.justice.digital.hmpps.entity.appointment

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,
    val description: String,
    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendance: Boolean,
)