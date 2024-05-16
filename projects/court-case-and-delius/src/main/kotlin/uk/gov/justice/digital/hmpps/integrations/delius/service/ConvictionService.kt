package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction

@Service
class ConvictionService() {
    fun convictionFor(offenderId: Long, convictionId: Long): Conviction? {

        return null
    }
}

