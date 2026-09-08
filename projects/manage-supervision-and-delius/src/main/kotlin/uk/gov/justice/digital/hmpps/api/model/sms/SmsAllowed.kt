package uk.gov.justice.digital.hmpps.api.model.sms

data class SmsAllowed(
    val crn: String,
    val smsAllowed: Boolean? = null
)
