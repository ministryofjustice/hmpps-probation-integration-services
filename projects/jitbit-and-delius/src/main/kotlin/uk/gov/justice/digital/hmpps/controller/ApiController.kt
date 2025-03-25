package uk.gov.justice.digital.hmpps.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.LimitedAccessDetail
import uk.gov.justice.digital.hmpps.model.limitedAccessDetail
import uk.gov.justice.digital.hmpps.service.CaseDetailService
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
class ApiController(
    private val caseDetailService: CaseDetailService,
    private val userAccessService: UserAccessService,
) {
    @GetMapping(value = ["/case/{crn}"])
    @PreAuthorize("hasRole('PROBATION_API__JITBIT__CASE_DETAIL')")
    fun getCaseDetails(@PathVariable crn: String): ResponseEntity<*> =
        if (checkAccess(crn).isLimitedAccess) ResponseEntity(
            mapOf("message" to "Access has been denied as this case is a Limited Access case."),
            HttpStatus.FORBIDDEN
        )
        else ResponseEntity.ok(caseDetailService.getCaseDetails(crn))

    @GetMapping(value = ["/case/{crn}/access"])
    @PreAuthorize("hasRole('PROBATION_API__JITBIT__CASE_DETAIL')")
    fun caseAccess(@PathVariable crn: String): LimitedAccessDetail = checkAccess(crn)

    private fun checkAccess(crn: String) =
        userAccessService.checkLimitedAccessFor(listOf(crn)).access.single { it.crn == crn }.limitedAccessDetail()
}
