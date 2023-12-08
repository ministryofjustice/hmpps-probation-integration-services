package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.CaseConvictions
import uk.gov.justice.digital.hmpps.api.model.CaseDetail
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.api.model.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.service.ConvictionService
import uk.gov.justice.digital.hmpps.service.ManagerService
import uk.gov.justice.digital.hmpps.service.PersonService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationCaseResource(
    private val managerService: ManagerService,
    private val personService: PersonService,
    private val convictionService: ConvictionService,
) {
    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("responsible-officer")
    fun findResponsibleOfficer(
        @PathVariable crn: String,
    ): ResponsibleOfficer =
        managerService.findResponsibleCommunityManager(crn)

    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("identifiers")
    fun findIdentifiers(
        @PathVariable crn: String,
    ): CaseIdentifier = personService.findIdentifiers(crn)

    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("details")
    fun findDetails(
        @PathVariable crn: String,
    ): ResponseEntity<CaseDetail> =
        personService.findDetailsFor(crn)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("convictions")
    fun findConvictions(
        @PathVariable crn: String,
    ): CaseConvictions = convictionService.findConvictions(crn)

    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("convictions/{id}")
    fun findConviction(
        @PathVariable crn: String,
        @PathVariable id: Long,
    ) = convictionService.findConviction(crn, id)
}
