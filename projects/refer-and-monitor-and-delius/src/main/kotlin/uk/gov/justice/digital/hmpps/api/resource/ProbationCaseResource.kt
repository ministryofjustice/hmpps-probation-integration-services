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
    private val convictionService: ConvictionService
) {
    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL')")
    @GetMapping("responsible-officer")
    fun findResponsibleOfficer(@PathVariable crn: String): ResponsibleOfficer =
        managerService.findResponsibleCommunityManager(crn)

    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL')")
    @GetMapping("identifiers")
    fun findIdentifiers(@PathVariable crn: String): CaseIdentifier = personService.findIdentifiers(crn)

    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL')")
    @GetMapping("details")
    fun findDetails(@PathVariable crn: String): ResponseEntity<CaseDetail> =
        personService.findDetailsFor(crn)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL')")
    @GetMapping("convictions")
    fun findConvictions(@PathVariable crn: String): CaseConvictions = convictionService.findConvictions(crn)

    @PreAuthorize("hasAnyRole('CRS_REFERRAL','PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL')")
    @GetMapping("convictions/{id}")
    fun findConviction(@PathVariable crn: String, @PathVariable id: Long) = convictionService.findConviction(crn, id)
}
