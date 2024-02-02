package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.service.AddressService
import uk.gov.justice.digital.hmpps.service.ManagerService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationCaseResource(
    private val responsibleManagerService: ManagerService,
    private val addressService: AddressService
) {
    @PreAuthorize("hasAnyRole('CVL_CONTEXT','PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("responsible-community-manager")
    fun findCommunityManager(@PathVariable crn: String): Manager = responsibleManagerService.findCommunityManager(crn)

    @PreAuthorize("hasAnyRole('CVL_CONTEXT','PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("addresses")
    fun findAddresses(@PathVariable crn: String): List<Address> = addressService.findAddresses(crn)
}
