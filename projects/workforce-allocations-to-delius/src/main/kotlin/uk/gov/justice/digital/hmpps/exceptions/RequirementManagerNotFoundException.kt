package uk.gov.justice.digital.hmpps.exceptions

import java.time.ZonedDateTime

class RequirementManagerNotFoundException(requirementId: Long, dateTime: ZonedDateTime) :
    RuntimeException("Requirement Manager Not Found for $requirementId at $dateTime")
