package uk.gov.justice.digital.hmpps.controller.personaldetails

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PersonController(val service: PersonalDetailsService) {

    @PreAuthorize("hasAnyRole('UPW_DETAILS','PROBATION_API__UPW__CASE_DETAIL')")
    @Operation(
        summary = """Details of all active personal contacts and personal
            circumstances for a person on probation""",
        description = """Details of all active personal contacts and personal
            circumstances related to the probation case identified by the CRN
            provided in the request. The service will return personal contacts
            and personal circumstances that are active on the date of the
            request. Used to access any additional information held in Delius
            when cloning an existing assessment
        """
    )
    @GetMapping(value = ["/case-data/{crn}/personal-details"])
    fun personDetails(
        @PathVariable crn: String
    ) = service.getPersonalDetails(crn)
}
