package uk.gov.justice.digital.hmpps.api.model.user

import jakarta.validation.constraints.Size

data class ClearAlerts(@Size(min = 1, max = 100) val alertIds: List<Long>)