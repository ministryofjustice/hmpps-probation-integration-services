package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class ExistingReferrals(val crn: String, val referrals: List<ApReferral>)

data class ApReferral(
    val referralDate: LocalDate,
    val expectedArrivalDate: LocalDate?,
    val expectedDepartureDate: LocalDate?,
    val decisionDate: ZonedDateTime?,
    val approvedPremises: ApprovedPremises
)

data class ApprovedPremises(val description: String)

data class ReferralDetail(
    val referral: ApReferral,
    val arrivedAt: ZonedDateTime? = null,
    val departedAt: ZonedDateTime? = null
)