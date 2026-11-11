package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.StaffService

@RestController
@PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__CASE_DETAIL')")
class StaffController(private val staffService: StaffService) {
    @GetMapping("/staff/{code}")
    fun getStaffByCode(@PathVariable code: String) = staffService.getStaffByCode(code)
}
