package uk.gov.justice.digital.hmpps.sevice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sevice.model.PersonMatch

@Service
class MatchWriter(
    private val personRepository: PersonRepository,
    private val custodyRepository: CustodyRepository
) {
    @Transactional
    fun update(personMatch: PersonMatch, custody: Custody?, onException: (Exception) -> Unit) = try {
        personMatch.match?.let {
            personRepository.save(personMatch.person.apply { it.prisonerNumber })
            custody?.apply { this.bookingRef = it.bookingNumber }?.let(custodyRepository::save)
        }
    } catch (e: Exception) {
        onException(e)
    }
}