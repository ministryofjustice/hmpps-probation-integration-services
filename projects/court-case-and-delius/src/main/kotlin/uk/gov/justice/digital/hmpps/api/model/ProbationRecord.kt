package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReportType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdRequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import java.time.LocalDate
import java.time.ZonedDateTime

data class ProbationRecord(
    val crn: String,
    val offenderManagers: List<OffenderManager>,
    val convictions: List<Conviction>
)

data class OffenderManager(
    val staff: Staff,
    val allocatedDate: LocalDate,
    val team: Team,
    val provider: String,
    val active: Boolean
)

fun PersonManager.toOffenderManager() =
    OffenderManager(staff.toStaff(), date.toLocalDate(), team.toTeam(), provider.description, true)

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff.toStaff() =
    Staff(listOfNotNull(forename, forename2).joinToString(" "), surname)

fun uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team.toTeam() =
    Team(description, telephone, ldu.description, district.description)

fun ReferenceData.keyValueOf() = KeyValue(code, description)
fun RequirementMainCategory.keyValueOf() = KeyValue(code, description)
fun AdRequirementMainCategory.keyValueOf() = KeyValue(code, description)
fun CourtReportType.keyValueOf() = KeyValue(code, description)

data class Staff(
    val forenames: String,
    val surname: String
)

data class Team(
    val description: String,
    val telephone: String? = null,
    val localDeliveryUnit: String,
    val district: String
)

data class Conviction(
    val active: Boolean = false,
    val inBreach: Boolean = false,
    val awaitingPsr: Boolean = false,
    val convictionDate: LocalDate?,
    val offences: List<Offence> = listOf(),
    val sentence: Sentence?,
    val custodialType: KeyValue?,
    val documents: List<OffenderDocumentDetail> = listOf(),
    val breaches: List<Breach> = listOf(),
    val requirements: List<Requirement> = listOf(),
    val pssRequirements: List<PssRequirement> = listOf(),
    val licenceConditions: List<LicenceCondition> = listOf(),
    val courtReports: List<CourtReport> = listOf()
)

data class CourtReport(
    val requestedDate: LocalDate,
    val requiredDate: LocalDate,
    val completedDate: LocalDate?,
    val courtReportType: KeyValue?,
    val deliveredCourtReportType: KeyValue?,
    val author: ReportAuthor?
)

data class ReportAuthor(
    val unallocated: Boolean,
    val forenames: String?,
    val surname: String?
)

data class Sentence(
    val description: String,
    val length: Long?,
    val lengthUnits: String?,
    val lengthInDays: Long?,
    val terminationDate: LocalDate?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val terminationReason: String?
)

data class Offence(
    val description: String,
    val main: Boolean = false,
    val offenceDate: LocalDate?
)

data class KeyValue(
    val code: String,
    val description: String
)

data class OffenderDocumentDetail(

    val documentName: String,
    val author: String?,
    val type: DocumentType,
    val extendedDescription: String?,
    val createdAt: ZonedDateTime?,
    val subType: KeyValue?
)

enum class DocumentType(val description: String) {
    OFFENDER_DOCUMENT("Offender related"),
    CONVICTION_DOCUMENT("Sentence related"),
    CPSPACK_DOCUMENT("Crown Prosecution Service case pack"),
    PRECONS_DOCUMENT("PNC previous convictions"),
    COURT_REPORT_DOCUMENT("Court report"),
    INSTITUTION_REPORT_DOCUMENT("Institution report"),
    ADDRESS_ASSESSMENT_DOCUMENT("Address assessment related document"),
    APPROVED_PREMISES_REFERRAL_DOCUMENT("Approved premises referral related document"),
    ASSESSMENT_DOCUMENT("Assessment document"),
    CASE_ALLOCATION_DOCUMENT("Case allocation document"),
    PERSONAL_CONTACT_DOCUMENT("Personal contact related document"),
    REFERRAL_DOCUMENT("Referral related document"),
    NSI_DOCUMENT("Non Statutory Intervention related document"),
    PERSONAL_CIRCUMSTANCE_DOCUMENT("Personal circumstance related document"),
    UPW_APPOINTMENT_DOCUMENT("Unpaid work appointment document"),
    CONTACT_DOCUMENT("Contact related document")
}

class Breach(
    val description: String?,
    val status: String?,
    val started: LocalDate?,
    val statusDate: LocalDate?
)

class PssRequirement(
    val description: String?,
    val subTypeDescription: String?
)

data class Requirement(
    val commencementDate: LocalDate?,
    val terminationDate: LocalDate?,
    val expectedStartDate: LocalDate?,
    val expectedEndDate: LocalDate?,
    val active: Boolean = false,
    val requirementTypeMainCategory: KeyValue?,
    val requirementTypeSubCategory: KeyValue?,
    val adRequirementTypeMainCategory: KeyValue?,
    val adRequirementTypeSubCategory: KeyValue?,
    val terminationReason: KeyValue?,
    val length: Long?
)

data class LicenceCondition(
    val description: String?,
    val subTypeDescription: String?,
    val startDate: LocalDate?,
    val notes: String?,
    val active: Boolean
)
