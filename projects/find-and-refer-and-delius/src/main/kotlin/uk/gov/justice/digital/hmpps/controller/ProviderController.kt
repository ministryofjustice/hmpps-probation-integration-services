package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ProviderService

@RestController
@Tag(name = "Probation Delivery Unit")
@RequestMapping("/providers")
@PreAuthorize("hasRole('PROBATION_API__FIND_AND_REFER__CASE_DETAIL')")
class ProviderController(private val providerService: ProviderService) {

    @GetMapping
    @Operation(summary = "Returns a list of providers")
    fun getProviders() = providerService.getProviders()

    @GetMapping("/{providerCode}/pdus")
    @Operation(summary = "Returns a list of pdus for a given provider code")
    fun getPdus(@PathVariable providerCode: String) = providerService.getPdus(providerCode)

    @GetMapping("/{providerCode}/pdus/{pduCode}/locations")
    @Operation(summary = "Returns a list of office locations for a given provider code and pdu code")
    fun getPduOfficeLocations(@PathVariable providerCode: String, @PathVariable pduCode: String) =
        providerService.getPduLocations(providerCode, pduCode)
}
