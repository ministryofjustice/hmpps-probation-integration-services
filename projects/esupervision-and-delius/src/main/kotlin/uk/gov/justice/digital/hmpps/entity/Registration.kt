package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@Table(name = "registration")
@SQLRestriction("deregistered = 0 and soft_deleted = 0")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
)

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long,

    @Column
    val code: String,
) {
    companion object {
        const val CONTACT_SUSPENDED_TYPE_CODE = "PRC"
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun existsByPersonIdAndTypeCode(personId: Long, typeCode: String): Boolean

    @Query("select r.personId from Registration r where r.personId in :personIds and r.type.code = :typeCode")
    fun findPersonIdsWithActiveType(personIds: Collection<Long>, typeCode: String): Set<Long>
}
