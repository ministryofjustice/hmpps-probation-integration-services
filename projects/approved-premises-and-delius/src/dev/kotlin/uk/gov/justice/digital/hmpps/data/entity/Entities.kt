package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.*
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

@Entity
class AddressAssessment(@Id val addressAssessmentId: Long, val assessmentDate: LocalDate)

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

@Entity(name = "ContactTypeEntity")
@Table(name = "r_contact_type")
class ContactType(@Id @Column(name = "contact_type_id") val id: Long, val description: String)

@Entity
class CourtAppearance(@Id val courtAppearanceId: Long, val courtId: Long, val eventId: Long)

@Entity
class Court(@Id val courtId: Long, val courtName: String)

@Entity
@Table(name = "r_court_report_type")
class CourtReportType(@Id val courtReportTypeId: Long, val description: String)

@Entity
class CourtReport(
    @Id val courtReportId: Long,
    val courtReportTypeId: Long,
    val courtAppearanceId: Long,
    val dateRequested: LocalDate
)

@Entity
class Custody(@Id val custodyId: Long, val disposalId: Long)

@Entity
class Disposal(@Id val disposalId: Long, val eventId: Long)

@Entity
@Table(name = "r_institution")
class Institution(
    @Id
    val institutionId: Long,
    val institutionName: String,
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,
)

@Entity
class InstitutionalReport(
    @Id
    val institutionalReportId: Long,
    val institutionReportTypeId: Long,
    val institutionId: Long,
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,
    val custodyId: Long,
    val dateRequested: LocalDate
)

@Entity(name = "NsiEntity")
@Table(name = "nsi")
class Nsi(@Id @Column(name = "nsi_id") val id: Long, val eventId: Long?)

@Entity(name = "PersonalCircumstanceEntity")
@Table(name = "personal_circumstance")
class PersonalCircumstance(@Id @Column(name = "personal_circumstance_id") val id: Long, val startDate: LocalDate)

@Entity
class PersonalContact(@Id val personalContactId: Long, val relationshipTypeId: Long, val relationship: String)

@Entity(name = "ReferralEntity")
@Table(name = "referral")
class Referral(@Id val referralId: Long, val referralTypeId: Long, val referralDate: LocalDate, val eventId: Long)

@Entity
@Table(name = "r_referral_type")
class ReferralType(@Id val referralTypeId: Long, val description: String)

@Entity
class UpwAppointment(
    @Id val upwAppointmentId: Long,
    val upwDetailsId: Long,
    val upwProjectId: Long,
    val appointmentDate: LocalDate
)

@Entity
class UpwDetails(@Id val upwDetailsId: Long, val disposalId: Long)

@Entity
class UpwProject(@Id val upwProjectId: Long, val name: String)
