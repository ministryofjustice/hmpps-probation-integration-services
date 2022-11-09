package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@Service
class DocumentService(private val documentRepository: DocumentRepository, private val personRepository: PersonRepository) {

    fun getDocumentsByCrn(crn: String): List<PersonDocument> {
        val person = personRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        return documentRepository.findAllByPersonId(person.id).map { PersonDocument(it.id, it.name, it.findRelatedTo(), it.lastSaved, it.sensitive) }
    }
}
