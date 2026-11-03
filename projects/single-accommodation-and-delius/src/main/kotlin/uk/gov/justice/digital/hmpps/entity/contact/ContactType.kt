package uk.gov.justice.digital.hmpps.entity.contact

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,
    val code: String,
) {
    companion object {
        const val DUTY_TO_REFER_CREATED = "EADR"
        const val DUTY_TO_REFER_UPDATED = "EADT"
    }
}
