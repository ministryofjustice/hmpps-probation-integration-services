package uk.gov.justice.digital.hmpps.entity.contact

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

    val code: String,

    @Column(name = "national_standards_contact")
    @Convert(converter = YesNoConverter::class)
    val nationalStandards: Boolean,
) {
    companion object {
        const val APPOINTMENT = "CAPY"
        const val THREE_WAY_MEETING = "CAPZ"
        const val PRE_GROUP_ONE_TO_ONE_MEETING = "CAPW"
        const val SUPERVISION_TWO_THIRDS_POINT = "PRST02"
        const val LICENCE_SUPERVISION_TWO_THIRDS_POINT = "PRST03"
        const val REVIEW_ENFORCEMENT_STATUS = "ARWS"
        const val COMPONENT_TERMINATED = "ETER"
        const val COMPONENT_TRANSFER_REJECTED = "ETCX"
        const val ORDER_COMPONENT_COMMENCED = "ECOM"
    }
}