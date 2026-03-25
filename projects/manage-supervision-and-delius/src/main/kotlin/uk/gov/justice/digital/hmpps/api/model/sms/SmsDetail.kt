package uk.gov.justice.digital.hmpps.api.model.sms

data class SmsDetail(
    val smsMessage: String,
    val deliusExternalReference: String,
)
