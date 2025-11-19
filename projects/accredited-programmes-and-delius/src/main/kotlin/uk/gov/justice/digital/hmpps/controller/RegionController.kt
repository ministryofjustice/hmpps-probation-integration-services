package uk.gov.justice.digital.hmpps.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.PduOfficeLocations
import uk.gov.justice.digital.hmpps.model.RegionWithMembers
import uk.gov.justice.digital.hmpps.service.GetRegion

@RestController
@RequestMapping("/regions")
class RegionController(private val getRegion: GetRegion) {
    @GetMapping("/{code}/members")
    fun getRegionMembers(@PathVariable code: String): RegionWithMembers = getRegion.withMembers(code)

    @GetMapping("/pdu/{pdu}/office-locations")
    fun getPduOfficeLocations(@PathVariable pdu: String): PduOfficeLocations =
        getRegion.pduOfficeLocations(pdu)
}