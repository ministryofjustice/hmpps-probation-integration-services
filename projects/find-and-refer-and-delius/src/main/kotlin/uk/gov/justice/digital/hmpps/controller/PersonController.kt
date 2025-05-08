package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonService

@RestController
@Tag(name = "Person")
@RequestMapping("/person")
class PersonController(private val personService: PersonService) {

    @PreAuthorize("hasRole('PROBATION_API__FIND_AND_REFER__CASE_DETAIL')")
    @GetMapping(value = ["/find/{identifier}"])
    fun findPerson(
        @PathVariable identifier: String
    ) = personService.findPerson(identifier)
}
