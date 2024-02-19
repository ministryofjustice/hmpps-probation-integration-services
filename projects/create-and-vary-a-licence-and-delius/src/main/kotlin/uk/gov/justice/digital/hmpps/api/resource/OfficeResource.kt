package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.OfficeAddress
import uk.gov.justice.digital.hmpps.service.OfficeAddressService

@RestController
@RequestMapping("office")
class OfficeResource(
    private val officeAddressService: OfficeAddressService
) {

    @PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
    @GetMapping("addresses")
    fun findAddresses(
        @RequestParam(required = true) ldu: String,
        @RequestParam(required = true) officeName: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,

        ): ResultSet<OfficeAddress> =
        officeAddressService.findAddresses(ldu, officeName, PageRequest.of(page, size)).let {
            ResultSet(it.content, it.totalElements, it.totalPages, page, size)
        }
}

data class ResultSet<T>(
    val results: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)