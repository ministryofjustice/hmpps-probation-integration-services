package uk.gov.justice.digital.hmpps.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.SubjectAccessRequestsService

@RestController
@RequestMapping("probation-case/{crn}")
@PreAuthorize("hasRole('PROBATION_API__SUBJECT_ACCESS_REQUEST__DETAIL')")
class SubjectAccessRequestsController(private val subjectAccessRequestsService: SubjectAccessRequestsService) {

    @GetMapping
    fun getPersonalDetails(@PathVariable crn: String) = subjectAccessRequestsService.getPersonDetailsByCrn(crn)
}
