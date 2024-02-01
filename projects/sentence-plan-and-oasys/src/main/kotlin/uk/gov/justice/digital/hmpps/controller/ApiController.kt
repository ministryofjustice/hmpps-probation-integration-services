package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController {
    @PreAuthorize("hasAnyRole('ROLE_SENTENCE_PLAN_RW','PROBATION_API__SENTENCE_PLAN__CASE_DETAIL')")
    @GetMapping(value = ["/needs/{crn}"])
    fun getNeeds(
        @PathVariable("crn") crn: String
    ) = genericNeeds(crn)

    private fun genericNeeds(crn: String): Needs {
        return Needs(
            listOf(
                Need("accommodation", "Accommodation"),
                Need("alcohol", "Alcohol misuse"),
                Need("attitudes", "Attitudes"),
                Need("drugs", "Drug misuse"),
                Need("employability", "Education, training and employment"),
                Need("lifestyle", "Lifestyle"),
                Need("relationships", "Relationships"),
                Need("behaviour", "Thinking and behaviour")
            )
        )
    }
}
