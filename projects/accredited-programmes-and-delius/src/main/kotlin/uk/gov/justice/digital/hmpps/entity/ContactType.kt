package uk.gov.justice.digital.hmpps.entity

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

    @Column
    val code: String,
) {
    companion object {
        const val SUPERVISION_TWO_THIRDS_POINT = "PRST02"
        const val LICENCE_SUPERVISION_TWO_THIRDS_POINT = "PRST03"
    }
}