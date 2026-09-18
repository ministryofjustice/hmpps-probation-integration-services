package uk.gov.justice.digital.hmpps.controller


import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.service.ReferenceDataService

@RestController
@RequestMapping("/regions")
class RegionController(
    private val referenceDataService: ReferenceDataService
) {
    @PreAuthorize("hasRole('PROBATION_API__EMDI__REFERENCE_DATA')")
    @GetMapping
    fun getRegions(): List<CodedValue> = referenceDataService.regions()


    @PreAuthorize("hasRole('PROBATION_API__EMDI__REFERENCE_DATA')")
    @GetMapping(value = ["/{regionCode}/pdu"])
    fun getPdus(@PathVariable("regionCode") regionCode: String): List<CodedValue> = referenceDataService.pdus(regionCode)
}
