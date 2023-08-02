package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/duty-to-refer-nsi")
class ApiController(private val service: ResettlementPassportService) {

    @PreAuthorize("hasRole('ROLE_RESETTLEMENT_PASSPORT')")
    @GetMapping("/{value}")
    fun findPerson(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType
    ) = service.getDutyToReferNSI(value, type)
    enum class IdentifierType {
        CRN, NOMS
    }
}
