package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ProviderService

@RestController
@Tag(name = "Providers")
@PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISIONS_API')")
class ProviderController(private val providerService: ProviderService) {
    @GetMapping("/provider/{code}")
    @Operation(summary = "Provider details by code")
    fun getProvider(
        @PathVariable code: String,
    ) = providerService.getProviderByCode(code)

    @GetMapping("/provider")
    @Operation(summary = "List active providers")
    fun getProviders() = providerService.getProviders()
}
