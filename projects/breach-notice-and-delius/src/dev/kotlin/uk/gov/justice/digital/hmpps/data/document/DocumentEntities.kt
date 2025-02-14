package uk.gov.justice.digital.hmpps.data.document

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class ApprovedPremisesReferral(@Id val approvedPremisesReferralId: Long, val eventId: Long)

@Entity
class Assessment(@Id val assessmentId: Long, val referralId: Long?)

@Entity
class CaseAllocation(@Id val caseAllocationId: Long, val eventId: Long)

@Entity
class CourtAppearance(@Id val courtAppearanceId: Long, val eventId: Long)

@Entity
class CourtReport(@Id val courtReportId: Long, val courtAppearanceId: Long)

@Entity
class Custody(@Id val custodyId: Long, val disposalId: Long)

@Entity
class InstitutionalReport(@Id val institutionalReportId: Long, val custodyId: Long)

@Entity
class Nsi(@Id @Column(name = "nsi_id") val id: Long, @Column(name = "event_id") val eventId: Long?)

@Entity
class Referral(@Id val referralId: Long, val eventId: Long)

@Entity
class UpwAppointment(@Id val upwAppointmentId: Long, val upwDetailsId: Long)

@Entity
class UpwDetails(@Id val upwDetailsId: Long, val disposalId: Long)