package uk.gov.justice.digital.hmpps.exceptions

class RequirementNotActiveException(requirementId: Long) : RuntimeException("Requirement $requirementId not active")
