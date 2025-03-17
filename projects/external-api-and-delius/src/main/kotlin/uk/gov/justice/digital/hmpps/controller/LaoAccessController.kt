package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.CrnRequest
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("probation-cases")
@PreAuthorize("hasRole('PROBATION_API__HMPPS_API__CASE_DETAIL')")
class LaoAccessController(private val uas: UserAccessService) {
    @RequestMapping("access", method = [RequestMethod.GET, RequestMethod.POST])
    fun caseAccess(
        @RequestParam(required = false) username: String?,
        @Valid @RequestBody request: CrnRequest
    ): UserAccess = username?.let { uas.userAccessFor(it, request.crns) } ?: uas.checkLimitedAccessFor(request.crns)
}