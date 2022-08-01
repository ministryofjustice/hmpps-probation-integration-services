package uk.gov.justice.digital.hmpps.exceptions

class RequirementNotFoundException(requirementId: Long) :
    RuntimeException("Unable to find requirement with id $requirementId")
