package uk.gov.justice.digital.hmpps.data.document

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
class ApprovedPremisesReferral(@Id val approvedPremisesReferralId: Long, val eventId: Long)

@Immutable
@Entity
@Table(name = "event")
class Event(
	@Id
	@Column(name = "event_id")
	val eventId: Long,
	@Column(name = "offender_id")
	val offenderId: Long,
	@Column(name = "event_number")
	val eventNumber: String,
	@Column(name = "active_flag", columnDefinition = "number")
	@Convert(converter = NumericBooleanConverter::class)
	val activeFlag: Boolean = true,
	@Column(name = "soft_deleted", columnDefinition = "number")
	@Convert(converter = NumericBooleanConverter::class)
	val softDeleted: Boolean = false,
)

@Immutable
@Entity
class Custody(@Id val custodyId: Long, val eventId: Long, val disposalId: Long)

@Immutable
@Entity
class Assessment(@Id val assessmentId: Long, val referralId: Long?)

@Immutable
@Entity
class CaseAllocation(@Id val caseAllocationId: Long, val eventId: Long)

@Immutable
@Entity
class CourtReport(@Id val courtReportId: Long, val courtAppearanceId: Long)

@Immutable
@Entity
class InstitutionalReport(@Id val institutionalReportId: Long, val custodyId: Long)

@Immutable
@Entity
class Nsi(@Id val nsiId: Long, val eventId: Long?)

@Immutable
@Entity
class Referral(@Id val referralId: Long, val eventId: Long)

@Immutable
@Entity
class UpwDetails(@Id val upwDetailsId: Long, val disposalId: Long)

@Immutable
@Entity
class UpwAppointment(@Id val upwAppointmentId: Long, val eventId: Long, val upwDetailsId: Long)
