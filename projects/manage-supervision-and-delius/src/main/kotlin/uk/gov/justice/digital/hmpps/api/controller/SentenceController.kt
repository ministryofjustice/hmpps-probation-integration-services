package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.service.OrdersService
import uk.gov.justice.digital.hmpps.service.SentenceService

@RestController
@Tag(name = "Sentence")
@RequestMapping("/sentence/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class SentenceController(
    private val sentenceService: SentenceService,
    private val ordersService: OrdersService,
    private val offenceService: OffenceService
) {

    @GetMapping
    @Operation(summary = "Display active events")
    fun getOverview(@PathVariable crn: String) = sentenceService.getEvents(crn)

    @GetMapping("/previous-orders")
    @Operation(summary = "Display inactive events")
    fun getPreviousEvents(@PathVariable crn: String) = ordersService.getPreviousEvents(crn)

    @GetMapping("/offences/{eventNumber}")
    @Operation(summary = "Display additinal offence details")
    fun getAdditionalOffenceDetails(@PathVariable crn: String, @PathVariable eventNumber: String) =
        offenceService.getOffencesForPerson(crn, eventNumber)
}