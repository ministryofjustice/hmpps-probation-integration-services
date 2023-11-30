package uk.gov.justice.digital.hmpps.controller.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Immutable
@Table(name = "registration")
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
class RegistrationEntity(
    @Id
    @Column(name = "registration_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData?,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(columnDefinition = "number")
    val deregistered: Boolean = false
)

@Immutable
@Table(name = "r_register_type")
@Entity
class RegisterType(
    @Column
    val code: String,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData,

    @Id
    @Column(name = "register_type_id")
    val id: Long
)

interface RegistrationRepository : JpaRepository<RegistrationEntity, Long> {
    fun findByPersonIdOrderByDateDesc(personId: Long): List<RegistrationEntity>
}
