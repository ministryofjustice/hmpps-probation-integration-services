package uk.gov.justice.digital.hmpps.exceptions

import uk.gov.justice.digital.hmpps.exception.NotFoundException

class OffenderNotFoundException(nomsId: String) : NotFoundException("Offender", "nomsId", nomsId)
