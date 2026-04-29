package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
@Table(name = "registration")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person? = null,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deRegistered: Boolean = false
)

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long,

    val code: String,
    val description: String
) {
    companion object {
        val ROSH_CODES = listOf("RLRH", "RMRH", "RHRH", "RVHR")
    }
}
