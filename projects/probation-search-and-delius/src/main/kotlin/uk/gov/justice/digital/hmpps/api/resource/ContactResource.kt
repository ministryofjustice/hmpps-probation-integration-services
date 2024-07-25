package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ContactJsonResponse
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@RestController
@PreAuthorize("hasRole('PROBATION_API__PROBATION_SEARCH__CASE_DETAIL')")
class ContactResource(private val personRepository: PersonRepository) {
    @GetMapping("/case/{crn}/contacts")
    fun getContacts(@PathVariable crn: String) = personRepository.findByCrn(crn)
        ?.let { personRepository.getContacts(it.id) }
        ?.map { ContactJsonResponse(it.contactId, it.json.characterStream.readText()) }
        ?: throw NotFoundException("Person", "CRN", crn)
}