package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "registration")
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
class RegistrationEntity(
    @Id
    @Column(name = "registration_id")
    val id: Long,
    @Column(name = "registration_date")
    val startDate: LocalDate,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val case: CaseEntity,
    @ManyToOne
    @JoinColumn(name = "register_category_id", updatable = false)
    val category: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "register_type_id", updatable = false)
    val type: RegisterType,
    @ManyToOne
    @JoinColumn(name = "registration_level", updatable = false)
    val level: ReferenceData,
    val softDeleted: Boolean = false,
    val deregistered: Boolean = false,
)

@Entity
@Table(name = "r_register_type")
@Immutable
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long,
    @Column(name = "code")
    val code: String,
    @Column(name = "description")
    val description: String,
    @Column(name = "colour")
    val riskColour: String?,
)
