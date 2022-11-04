package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.model.BreachDetails
import uk.gov.justice.digital.hmpps.integrations.delius.model.Outcome
import uk.gov.justice.digital.hmpps.integrations.delius.model.Status
import uk.gov.justice.digital.hmpps.integrations.delius.repository.NsiRepository

@Service
class BreachDetailsService(
    private val nsiRepository: NsiRepository
) {
    fun getBreachDetails(nsiId: Long): BreachDetails {
        val nsi = nsiRepository.findById(nsiId).orElseThrow {
            NotFoundException("Nsi", "id", nsiId)
        }

        return BreachDetails(
            nsi.referralDate,
            nsi.outcome?.run { Outcome(code, description) },
            Status(nsi.status.code, nsi.status.description),
            nsi.event?.number
        )
    }
}
