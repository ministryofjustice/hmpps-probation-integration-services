package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.*

@RestController
@Tag(name = "Sentence")
@RequestMapping("/sentence/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class SentenceController(
    private val sentenceService: SentenceService,
    private val ordersService: OrdersService,
    private val offenceService: OffenceService,
    private val contactService: ContactService,
    private val licenceConditionService: LicenceConditionService
) {

    @GetMapping
    @Operation(summary = "Display active events")
    fun getOverview(
        @PathVariable crn: String,
        @RequestParam(required = false) number: String?,
    ) = sentenceService.getEvents(crn, number)

    @GetMapping("/previous-orders")
    @Operation(summary = "Display inactive events")
    fun getPreviousEvents(@PathVariable crn: String) = ordersService.getPreviousEvents(crn)

    @GetMapping("/offences/{eventNumber}")
    @Operation(summary = "Display additional offence details")
    fun getAdditionalOffenceDetails(@PathVariable crn: String, @PathVariable eventNumber: String) =
        offenceService.getOffencesForPerson(crn, eventNumber)

    @GetMapping("/contacts")
    @Operation(summary = "Display contacts")
    fun getContacts(@PathVariable crn: String) = contactService.getContacts(crn)

    @GetMapping("/licence-condition/{licenceConditionId}/note/{noteId}")
    @Operation(summary = "Get licence condition note")
    fun getLicenceConditionNote(
        @PathVariable crn: String,
        @PathVariable licenceConditionId: Long,
        @PathVariable noteId: Int
    ) = licenceConditionService.getLicenceConditionNote(crn, licenceConditionId, noteId)
}