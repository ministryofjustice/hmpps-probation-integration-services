package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.CossoEventDocuments
import uk.gov.justice.digital.hmpps.model.DocumentCrn
import uk.gov.justice.digital.hmpps.model.PersonDetails
import uk.gov.justice.digital.hmpps.service.DocumentService
import uk.gov.justice.digital.hmpps.service.PersonService
import java.util.*

@RestController
class CaseDetailsController(
    val personService: PersonService,
    val documentService: DocumentService,
) {
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping(value = ["/basic-details/{crn}/{username}"])
    fun getBasicDetails(@PathVariable("crn") crn: String, @PathVariable("username") username: String): PersonDetails =
        personService.getBasicDetails(crn, username)

    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping(value = ["/case/{cossoId}"])
    fun findCrnForCosso(@PathVariable("cossoId") cossoId: UUID): DocumentCrn = personService.crnFor(cossoId)

    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping(value = ["/cosso-event-documents/{crn}/{eventNumber}"])
    fun getCossoEventDocuments(
        @PathVariable("crn") crn: String, @PathVariable("eventNumber") eventNumber: String
    ): CossoEventDocuments = documentService.cossoIdsForEvent(crn, eventNumber)
}