package uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table
@Where(clause = "soft_deleted = 0 and deregistered = 0")
class Registration(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(columnDefinition = "number")
    val deregistered: Boolean,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_register_type")
class RegisterType(

    @Column
    val code: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        GANG_AFFILIATION("STRG"),
        SEX_OFFENCE("ARSO")
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun existsByPersonIdAndTypeCode(personId: Long, code: String): Boolean
}
