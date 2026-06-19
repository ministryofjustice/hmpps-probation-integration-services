package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.service.DetailsService
import uk.gov.justice.digital.hmpps.service.DocumentService
import uk.gov.justice.digital.hmpps.service.RegistrationsService
import java.util.*

@RestController
class SuicideRiskFormController(
    private val detailsService: DetailsService,
    private val registrationsService: RegistrationsService,
    private val documentService: DocumentService
) {

    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @GetMapping(value = ["/basic-details/{crn}"])
    fun getBasicDetails(@PathVariable crn: String): BasicDetails = detailsService.basicDetails(crn)

    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @GetMapping(value = ["/information-page/{crn}"])
    fun getInformationPage(@PathVariable crn: String): InformationPageResponse =
        registrationsService.informationPage(crn)

    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @GetMapping(value = ["/sign-and-send/{crn}/{username}"])
    fun getSignAndSend(@PathVariable crn: String, @PathVariable username: String): SignAndSendResponse =
        detailsService.signAndSend(crn, username)

    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @GetMapping(value = ["/case/{suicideRiskFormId}"])
    fun findCrnForSuicideRiskForm(@PathVariable suicideRiskFormId: UUID): DocumentCrn =
        detailsService.crnFor(suicideRiskFormId)

    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @PostMapping(value = ["/treatment"])
    fun getContactDocuments(@RequestBody contactIds: List<Long>): ContactDocumentResponse =
        documentService.listDocumentsForContacts(contactIds)
}
