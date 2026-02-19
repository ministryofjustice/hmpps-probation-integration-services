package uk.gov.justice.digital.hmpps.data.document

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Immutable
@Entity
class ApprovedPremisesReferral(@Id val approvedPremisesReferralId: Long, val eventId: Long)

@Immutable
@Entity
class Custody(@Id val custodyId: Long, val eventId: Long, val disposalId: Long)

@Immutable
@Entity
class Disposal(@Id val disposalId: Long, val eventId: Long)


@Immutable
@Entity
class Assessment(@Id val assessmentId: Long, val referralId: Long?)

@Immutable
@Entity
class CaseAllocation(@Id val caseAllocationId: Long, val eventId: Long)

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

@Immutable
@Entity
class Contact(@Id val contactId: Long, val eventId: Long)