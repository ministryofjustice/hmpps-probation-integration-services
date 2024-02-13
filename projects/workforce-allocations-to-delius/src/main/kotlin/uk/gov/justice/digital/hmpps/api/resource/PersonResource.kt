package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
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

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "Basic information on the person on probation",
        description = """Basic information on the person on probation as held in Delius,
            identified by either the CRN or NOMS number provided in the request. Supports
            display of the overview person information in the HMPPS Workload service
        """
    )
    @GetMapping("/{value}")
    fun findPerson(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType
    ): Person = personService.findByIdentifier(value, type)
}

enum class IdentifierType {
    CRN, NOMS
}
