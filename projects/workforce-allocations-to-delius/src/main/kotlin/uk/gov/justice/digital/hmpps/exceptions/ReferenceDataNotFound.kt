package uk.gov.justice.digital.hmpps.exceptions

class ReferenceDataNotFound(masterCode: String, code: String) :
    RuntimeException("ReferenceData Not Found $masterCode:$code")
