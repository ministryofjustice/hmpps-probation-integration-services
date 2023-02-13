package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.service.PersonService

@RestController
@RequestMapping("/person")
class PersonResource(private val personService: PersonService) {

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/{value}")
    fun findPerson(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType
    ): Person = personService.findByIdentifier(value, type)
}

enum class IdentifierType {
    CRN, NOMS
}