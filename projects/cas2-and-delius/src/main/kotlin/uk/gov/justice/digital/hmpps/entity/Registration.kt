package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Table(name = "registration")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    val registrationDate: LocalDate,
    val nextReviewDate: LocalDate?,

    @OneToOne(mappedBy = "registration", optional = true)
    val deregistration: Deregistration? = null,

    @OneToOne
    @JoinColumn(name = "register_type_id")
    val registerType: RegisterType,

    @OneToOne
    @JoinColumn(name = "registration_category")
    val category: ReferenceData,

    )

@Entity
@Table(name = "r_register_type")
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long = 0,

    val code: String,
    val description: String,
)

@Entity
@Table(name = "deregistration")
class Deregistration(
    @Id
    @Column(name = "deregistration_id")
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "registration_id", referencedColumnName = "registration_id")
    val registration: Registration,

    @Column(name = "deregistration_date")
    val endDate: LocalDate,
)

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        """
            select r from Registration r
            where r.registerType.code in :codes
            and r.person.crn = :crn
        """
    )
    fun findByRegistrationCodes(crn: String, codes: List<String>): List<Registration>
}
