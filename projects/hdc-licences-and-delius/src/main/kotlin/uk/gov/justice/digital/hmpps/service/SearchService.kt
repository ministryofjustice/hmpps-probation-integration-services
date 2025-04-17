package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@Service
class SearchService (private val personRepository: PersonRepository) {

    fun findByListOfNoms(nomsList: List<String>) {
        personRepository.findByNomsNumberIn(nomsList)
    }
}