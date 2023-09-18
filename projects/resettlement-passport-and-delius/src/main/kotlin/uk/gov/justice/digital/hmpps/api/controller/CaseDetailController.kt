package uk.gov.justice.digital.hmpps.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifiers
import uk.gov.justice.digital.hmpps.api.model.MappaDetail
import uk.gov.justice.digital.hmpps.service.CaseDetailService

@RestController
@RequestMapping("/probation-cases")
class CaseDetailController(private val caseDetailService: CaseDetailService) {

    @PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_SUPERVISION_ACCESS')")
    @GetMapping("/{nomsId}/crn")
    fun findCrn(@PathVariable nomsId: String): CaseIdentifiers = caseDetailService.findCrnByNomsId(nomsId)

    @PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_SUPERVISION_ACCESS')")
    @GetMapping("/{crn}/mappa")
    fun findMappaInfo(@PathVariable crn: String): MappaDetail = caseDetailService.findMappaDetail(crn)

    @PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_SUPERVISION_ACCESS')")
    @GetMapping("/{crn}/community-manager")
    fun getCommunityManager(@PathVariable crn: String) = caseDetailService.findCommunityManager(crn)
}
