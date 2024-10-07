package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.*
import org.hibernate.type.YesNoConverter
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class ApprovedPremisesReferral(
    @Id val approvedPremisesReferralId: Long,
    val referralDate: LocalDate,
    val eventId: Long?
)

@Entity
class Assessment(
    @Id val assessmentId: Long,
    val assessmentTypeId: Long,
    val referralId: Long?,
    val assessmentDate: LocalDate
)

@Entity
@Table(name = "r_assessment_type")
class AssessmentType(@Id val assessmentTypeId: Long, val description: String)

@Entity
class CaseAllocation(@Id val caseAllocationId: Long, val eventId: Long)

@Entity
class InstitutionalReport(
    @Id
    val institutionalReportId: Long,
    val institutionReportTypeId: Long,
    val institutionId: Long,
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,
    val custodyId: Long,
    val dateRequested: LocalDate,
    val dateRequired: LocalDate,
    val dateCompleted: LocalDateTime,
)

@Entity
class PersonalCircumstance(@Id val personalCircumstanceId: Long, val circumstanceTypeId: Long, val startDate: LocalDate)

@Entity
@Table(name = "r_circumstance_type")
class PersonalCircumstanceType(@Id val circumstanceTypeId: Long, val codeDescription: String)

@Entity
class PersonalContact(@Id val personalContactId: Long, val relationshipTypeId: Long, val relationship: String)

@Entity
class Referral(@Id val referralId: Long, val referralTypeId: Long, val referralDate: LocalDate, val eventId: Long)

@Entity
@Table(name = "r_referral_type")
class ReferralType(@Id val referralTypeId: Long, val description: String)

@Entity
class UpwProject(@Id val upwProjectId: Long, val name: String)

@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val userId: Long,
    val forename: String,
    val surname: String
)

