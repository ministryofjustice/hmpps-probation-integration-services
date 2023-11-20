package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import java.util.stream.Stream

@Service
class CrnStreamingService(private val personRepository: PersonRepository) {
    fun getActiveCrns(): Stream<String> = personRepository.findAllCrns()
}
