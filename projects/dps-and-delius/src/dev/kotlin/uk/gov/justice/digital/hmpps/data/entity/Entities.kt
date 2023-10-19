package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
data class AddressAssessment(@Id val addressAssessmentId: Long, val assessmentDate: LocalDate)

@Entity
data class ApprovedPremisesReferral(@Id val approvedPremisesReferralId: Long, val referralDate: LocalDate, val eventId: Long?)

@Entity
data class Assessment(@Id val assessmentId: Long, val assessmentTypeId: Long, val referralId: Long?, val assessmentDate: LocalDate)

@Entity
@Table(name = "r_assessment_type")
data class AssessmentType(@Id val assessmentTypeId: Long, val description: String)

@Entity
data class CaseAllocation(@Id val caseAllocationId: Long, val eventId: Long)

@Entity
data class Contact(@Id val contactId: Long, val contactTypeId: Long, val eventId: Long?, val contactDate: LocalDate)

@Entity
@Table(name = "r_contact_type")
data class ContactType(@Id val contactTypeId: Long, val description: String)

@Entity
data class Court(@Id val courtId: Long, val courtName: String)

@Entity
@Table(name = "r_court_report_type")
data class CourtReportType(@Id val courtReportTypeId: Long, val description: String)

@Entity
data class CourtReport(@Id val courtReportId: Long, val courtReportTypeId: Long, val courtAppearanceId: Long, val dateRequested: LocalDate)

@Entity
data class InstitutionalReport(@Id val institutionalReportId: Long, val institutionReportTypeId: Long, val institutionId: Long, val establishment: String, val custodyId: Long, val dateRequested: LocalDate)

@Entity
data class Nsi(@Id val nsiId: Long, val nsiTypeId: Long, val eventId: Long?, val referralDate: LocalDate)

@Entity
@Table(name = "r_nsi_type")
data class NsiType(@Id val nsiTypeId: Long, val description: String)

@Entity
data class PersonalCircumstance(@Id val personalCircumstanceId: Long, val circumstanceTypeId: Long, val startDate: LocalDate)

@Entity
@Table(name = "r_circumstance_type")
data class PersonalCircumstanceType(@Id val circumstanceTypeId: Long, val codeDescription: String)

@Entity
data class PersonalContact(@Id val personalContactId: Long, val relationshipTypeId: Long, val relationship: String)

@Entity
data class Referral(@Id val referralId: Long, val referralTypeId: Long, val referralDate: LocalDate, val eventId: Long)

@Entity
@Table(name = "r_referral_type")
data class ReferralType(@Id val referralTypeId: Long, val description: String)

@Entity
data class UpwAppointment(@Id val upwAppointmentId: Long, val upwDetailsId: Long, val upwProjectId: Long, val appointmentDate: LocalDate)

@Entity
data class UpwDetails(@Id val upwDetailsId: Long, val disposalId: Long)

@Entity
data class UpwProject(@Id val upwProjectId: Long, val disposalId: Long, val name: String)

@Entity
@Table(name = "user_")
data class User(
    @Id
    @Column(name = "user_id")
    val userId: Long,
    val forename: String,
    val surname: String
)
