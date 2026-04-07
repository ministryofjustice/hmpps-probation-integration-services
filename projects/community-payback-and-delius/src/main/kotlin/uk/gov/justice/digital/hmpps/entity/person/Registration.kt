package uk.gov.justice.digital.hmpps.entity.person

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.util.List

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
@Table(name = "registration")
class Registration(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deRegistered: Boolean,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun findByPerson_CrnOrderByType_Code(personCrn: String): List<Registration>
}

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(

    val code: String,

    val description: String,

    val colour: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long,
)