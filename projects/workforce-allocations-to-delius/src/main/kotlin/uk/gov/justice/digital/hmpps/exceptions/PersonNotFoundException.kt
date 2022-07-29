package uk.gov.justice.digital.hmpps.exceptions

class PersonNotFoundException(crn: String) : RuntimeException("Offender Not Found : $crn")