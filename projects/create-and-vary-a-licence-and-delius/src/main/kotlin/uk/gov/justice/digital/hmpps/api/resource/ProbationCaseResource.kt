package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.ProbationCase
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.service.ManagerService

@RestController
@RequestMapping("probation-case")
@PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
class ProbationCaseResource(
    private val personRepository: PersonRepository,
    private val responsibleManagerService: ManagerService,
) {
    @GetMapping("{crnOrNomisId}")
    fun findCase(@PathVariable crnOrNomisId: String): ProbationCase =
        personRepository.findByCrnOrNomsNumber(crnOrNomisId)
            ?.let { ProbationCase(it.crn, it.nomsNumber, it.pncNumber, it.croNumber) }
            ?: throw NotFoundException("Probation case", "CRN or NOMIS id", crnOrNomisId)

    @PostMapping
    fun findCases(@RequestBody crnsOrNomisIds: List<String>): List<ProbationCase> =
        personRepository.findByCrnInOrNomsNumberIn(crnsOrNomisIds)
            .map { ProbationCase(it.crn, it.nomsNumber, it.pncNumber, it.croNumber) }

    @GetMapping("{crnOrNomisId}/responsible-community-manager")
    fun findCommunityManager(@PathVariable crnOrNomisId: String): Manager =
        responsibleManagerService.findCommunityManager(crnOrNomisId)

    @PostMapping("/responsible-community-manager")
    fun findCommunityManagerEmails(@RequestBody crnsOrNomisIds: List<String>): List<Manager> =
        responsibleManagerService.findCommunityManagers(crnsOrNomisIds)
}
