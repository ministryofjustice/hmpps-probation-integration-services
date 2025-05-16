package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentSearch
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentTextSearch
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.service.DocumentsService

@RestController
@Tag(name = "Documents")
@RequestMapping("/documents/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class DocumentsController(private val documentsService: DocumentsService) {

    @GetMapping
    @Operation(summary = "Gets all documents for a person")
    fun getPersonDocuments(
        @PathVariable crn: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "createdAt.desc") sortBy: String,
    ) = documentsService.getDocuments(crn, PageRequest.of(page, size, sort(sortBy)), sortBy)

    @PostMapping("/search")
    @Operation(summary = "Search for documents for a person by file name and created date")
    fun searchPersonDocuments(
        @PathVariable crn: String,
        @RequestBody @Valid documentSearch: DocumentSearch,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "createdAt.desc") sortBy: String,
    ) = documentsService.search(documentSearch, crn, PageRequest.of(page, size, sort(sortBy)), sortBy)

    @PostMapping("/search/text")
    @Operation(summary = "Search within documents for a person for a search query and created date")
    fun searchTextPersonDocuments(
        @PathVariable crn: String,
        @RequestBody @Valid documentTextSearch: DocumentTextSearch,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false) sortBy: String?,
    ) = documentsService.textSearch(documentTextSearch, crn, PageRequest.of(page, size, sort(sortBy)), sortBy)

    private fun sort(sortString: String?): Sort {
        if (sortString == null) {
            return Sort.unsorted()
        }
        val regex = Regex(pattern = "[A-Z]+\\.(ASC|DESC)", options = setOf(RegexOption.IGNORE_CASE))
        if (!regex.matches(sortString)) {
            throw InvalidRequestException("Sort criteria invalid format")
        }
        val sortBy = sortString.split(".")[0]
        val direction = sortString.split(".")[1].uppercase()
        return Sort.by(Sort.Direction.valueOf(direction), sortBy)
    }
}
