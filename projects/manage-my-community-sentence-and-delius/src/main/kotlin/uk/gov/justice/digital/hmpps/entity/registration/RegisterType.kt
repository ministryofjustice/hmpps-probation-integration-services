package uk.gov.justice.digital.hmpps.entity.registration

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

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
        const val CONTACT_SUSPENDED_TYPE_CODE = "PRC"
    }
}
