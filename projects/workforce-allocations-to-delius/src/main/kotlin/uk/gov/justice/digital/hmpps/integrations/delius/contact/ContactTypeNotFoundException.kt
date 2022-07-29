package uk.gov.justice.digital.hmpps.integrations.delius.contact

class ContactTypeNotFoundException(val code: String) : RuntimeException("Contact Type not found: $code")
