package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.DeliveryUnit
import uk.gov.justice.digital.hmpps.service.ProviderService

@RestController
@RequestMapping("/probation-delivery-units")
class PduResource(private val providerService: ProviderService) {
    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL__RW')")
    @GetMapping
    fun findAllDeliveryUnits(): List<DeliveryUnit> = providerService.findSelectableDeliveryUnits()
}
