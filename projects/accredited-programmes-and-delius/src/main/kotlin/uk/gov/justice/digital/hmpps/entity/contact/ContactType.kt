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

    @Column
    val code: String,
) {
    companion object {
        const val IAPS_APPOINTMENT = "CAPX"
        const val ACCREDITED_PROGRAMMES_APPOINTMENT = "???" // TODO update this when NDST provide the new code
        const val SUPERVISION_TWO_THIRDS_POINT = "PRST02"
        const val LICENCE_SUPERVISION_TWO_THIRDS_POINT = "PRST03"
    }
}