package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.SentenceService
import uk.gov.justice.digital.hmpps.service.SparksService

@RestController
@Tag(name = "Sparks")
@RequestMapping("/sparks")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class SparksController (private val sparksService: SparksService) {
    @GetMapping("/reference-data")
    fun getSparksReferenceData() = sparksService.getSparksReferenceData()
}