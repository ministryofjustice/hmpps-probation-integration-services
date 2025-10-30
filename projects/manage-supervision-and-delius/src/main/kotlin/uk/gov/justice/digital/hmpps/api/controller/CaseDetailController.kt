package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.service.GetProbationPractitioner

@RestController
@Tag(name = "Case Detail")
@RequestMapping("/case/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class CaseDetailController(private val getPp: GetProbationPractitioner) {
    @GetMapping("/probation-practitioner")
    fun findPractitionerFor(@PathVariable crn: String): ProbationPractitioner = getPp.forCrn(crn)
}