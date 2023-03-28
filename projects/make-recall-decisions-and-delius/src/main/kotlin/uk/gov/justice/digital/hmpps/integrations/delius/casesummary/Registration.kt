package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Table(name = "registration")
@Entity(name = "CaseSummaryRegistration")
@Where(clause = "soft_deleted = 0 and deregistered = 0")
class Registration(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegistrationType,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(columnDefinition = "number")
    val deregistered: Boolean = false
)

@Immutable
@Table(name = "r_register_type")
@Entity(name = "CaseSummaryRegistrationType")
class RegistrationType(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column
    val description: String
)

interface CaseSummaryRegistrationRepository : JpaRepository<Registration, Long> {
    @Query("select r.type.description from CaseSummaryRegistration r where r.personId = :personId")
    fun findTypeDescriptionsByPersonId(personId: Long): List<String>
}
