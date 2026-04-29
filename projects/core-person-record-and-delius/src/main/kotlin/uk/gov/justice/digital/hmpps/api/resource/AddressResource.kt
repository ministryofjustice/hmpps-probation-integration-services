package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.AddressService

@RestController
@PreAuthorize("hasRole('PROBATION_API__CORE_PERSON__CASE_DETAIL')")
class AddressResource(private val addressService: AddressService) {
    @GetMapping(value = ["/address/{id}"])
    fun getAddress(@PathVariable id: Long) = addressService.getAddress(id)
}
