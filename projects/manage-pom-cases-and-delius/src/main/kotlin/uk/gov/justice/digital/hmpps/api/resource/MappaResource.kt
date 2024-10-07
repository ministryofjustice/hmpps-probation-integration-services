package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.services.MappaService

@RestController
class MappaResource(val mappaService: MappaService) {
    @PreAuthorize("hasRole('PROBATION_API__MANAGE_POM_CASES__CASE_DETAIL')")
    @GetMapping(value = ["/case-records/{crn}/risks/mappa"])
    fun getMappaDetails(
        @PathVariable("crn") crn: String
    ) = mappaService.getMappaDetail(crn)
}
