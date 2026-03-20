package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Table(name = "registration")
@SQLRestriction("soft_deleted = 0")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long,

    @ManyToOne
    val person: Person,

    @Lob
    @Column(name = "registration_notes")
    val notes: String?,

    @Column(name = "registration_date")
    val startDate: LocalDate,

    @OneToOne(mappedBy = "registration", optional = true)
    var deregistration: Deregistration? = null,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean,

    @Column(name = "document_linked", columnDefinition = "char(1)")
    @Convert(converter = YesNoConverter::class)
    val documentLinked: Boolean,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val registerLevel: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val registerCategory: ReferenceData,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

@Entity
@Table(name = "r_register_type")
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long,
    @Column(name = "code")
    val code: String,
    @Column(name = "description")
    val description: String
)

@Entity
@Table(name = "deregistration")
class Deregistration(
    @Id
    @Column(name = "deregistration_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "registration_id", referencedColumnName = "registration_id")
    val registration: Registration,

    @Column(name = "deregistration_date")
    val date: LocalDate,
)

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        """
        select r from Registration r
        where r.type.code in :codes
        and r.person.crn = :crn
    """
    )
    fun findRegistrationsByCrn(crn: String, codes: List<String>): List<Registration>
}