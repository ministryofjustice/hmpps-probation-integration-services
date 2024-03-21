package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.PersonDetail
import uk.gov.justice.digital.hmpps.service.PersonService

@RestController
@RequestMapping("probation-cases")
class PersonResource(private val personService: PersonService) {
    @PreAuthorize("hasRole('PROBATION_API__CORE_PERSON__CASE_DETAIL')")
    @GetMapping(value = ["/{identifier}"])
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
}
