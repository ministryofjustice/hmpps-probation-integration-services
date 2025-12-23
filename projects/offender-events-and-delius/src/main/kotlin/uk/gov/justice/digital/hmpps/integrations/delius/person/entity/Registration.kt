package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Immutable
@Entity
@Table(name = "registration")
@SQLRestriction("deregistered = 0 and soft_deleted = 0")
class Registration(

    @Id
    @Column(name = "registration_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registration_id_seq")
    val id: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData? = null,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deRegistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

@Immutable
@Table(name = "r_register_type")
@Entity
class RegisterType(

    @Id
    @Column(name = "register_type_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    ) {
    enum class Code(val value: String) {
        MAPPA("MAPP")
    }
}

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @EntityGraph(attributePaths = ["type", "category"])
    fun findByPersonIdAndTypeCodeOrderByIdDesc(personId: Long, typeCode: String): List<Registration>
}
