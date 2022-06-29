package uk.gov.justice.digital.hmpps.exceptions

class CaseNoteTypeNotFoundException(type: String) : RuntimeException("Case note type not found for: $type")
