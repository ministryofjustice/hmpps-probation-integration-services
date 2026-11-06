package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonSentenceService

@RestController
class PersonSentenceController(
    private val personSentenceService: PersonSentenceService
) {
    @PreAuthorize("hasRole('PROBATION_API__COMMUNITY_SUPPORT_INTERVENTIONS__CASE_DETAIL')")
    @GetMapping(value = ["/case/{personIdentifier}/offence-and-sentence"])
    fun getPersonSentenceByCrn(@PathVariable("personIdentifier") personIdentifier: String) =
        personSentenceService.getPersonSentenceForCrn(personIdentifier)
}