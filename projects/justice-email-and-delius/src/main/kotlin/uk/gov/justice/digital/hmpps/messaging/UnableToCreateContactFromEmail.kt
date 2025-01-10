package uk.gov.justice.digital.hmpps.messaging

data class UnableToCreateContactFromEmail(val email: EmailMessage, val reason: String)