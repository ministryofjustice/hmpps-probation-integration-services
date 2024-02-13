package uk.gov.justice.digital.hmpps.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ResettlementPassportService

@RestController
@RequestMapping("/duty-to-refer-nsi")
class DutyToReferController(private val service: ResettlementPassportService) {

    @PreAuthorize("hasRole('PROBATION_API__RESETTLEMENT_PASSPORT__CASE_DETAIL')")
    @GetMapping("/{value}")
    fun findPerson(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType
    ) = service.getDutyToReferNSI(value, type)

    enum class IdentifierType {
        CRN, NOMS
    }
}
