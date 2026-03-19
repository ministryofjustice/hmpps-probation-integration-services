package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "registration_notes")
    val registrationNotes: String?,

    @Column(name = "registration_date")
    val startDate: LocalDate,

    @Column(name = "deregistration_date")
    val endDate: LocalDate?,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean,

    @Column(name = "document_linked", columnDefinition = "char(1)")
    @Convert(converter = YesNoConverter::class)
    val documentLinked: Boolean,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val registerType: ReferenceData,

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

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        """
        select r from Registration r
        where r.registerType.code in :codes
        and r.person.crn = :crn
        and r.softDeleted = false
    """
    )
    fun findRegistrationsByCrn(crn: String, codes: List<String>): List<Registration>
}