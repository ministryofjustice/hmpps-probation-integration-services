package uk.gov.justice.digital.hmpps.audit

class BusinessInteractionNotFoundException(code: String) :
    RuntimeException("No Business Interaction found with code $code")
