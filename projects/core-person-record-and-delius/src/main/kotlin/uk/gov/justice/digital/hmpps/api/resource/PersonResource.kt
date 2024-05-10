package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.PersonDetail
import uk.gov.justice.digital.hmpps.service.PersonService

@RestController
@PreAuthorize("hasRole('PROBATION_API__CORE_PERSON__CASE_DETAIL')")
class PersonResource(private val personService: PersonService) {
    @GetMapping(value = ["/probation-cases/{identifier}"])
    fun getPersonDetails(
        @PathVariable identifier: String
    ): PersonDetail {
        val id = identifier.toLongOrNull()
        return if (id == null) {
            personService.getPersonDetail(identifier)
        } else {
            personService.getPersonDetail(id)
        }
    }

    @GetMapping(value = ["/all-probation-cases"])
    fun getPersonDetails(pageable: Pageable) = personService.getAllPersonDetails(pageable)
}
