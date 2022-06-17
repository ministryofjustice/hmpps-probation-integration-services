package uk.gov.justice.digital.hmpps.exceptions

class OffenderNotFoundException(nomsId: String) : RuntimeException("Offender not found: $nomsId")