package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.sevice.PersonService

@RestController
@RequestMapping("person")
class PersonUpdateController(private val personService: PersonService) {

    // @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_STAFF')")
    @RequestMapping(value = ["/populate-noms-number"], method = [RequestMethod.GET, RequestMethod.POST])
    fun populateNomsNumbers(
        @RequestParam(defaultValue = "false") trialOnly: Boolean,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>
    ) = personService.populateNomsNumber(crns, trialOnly)
}
