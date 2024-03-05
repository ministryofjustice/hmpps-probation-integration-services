package uk.gov.justice.digital.hmpps.integrations.delius.registration.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "registration")
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
class Registration(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "deregistered", columnDefinition = "number")
    val deRegistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

@Immutable
@Table(name = "r_register_type")
@Entity
class RegisterType(
    @Column
    val code: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData?,

    @Column
    val description: String,

    val colour: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long
)


interface RegistrationRepository : JpaRepository<Registration, Long> {
    @EntityGraph(attributePaths = ["person", "type.flag"])
    fun findAllByPersonCrn(crn: String): List<Registration>
}