package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.service.DocumentsService

@RestController
@Tag(name = "Documents")
@RequestMapping("/documents/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class DocumentsController(private val documentsService: DocumentsService) {

    @GetMapping
    @Operation(summary = "Gets all documents for a person")
    fun getPersonActivity(
        @PathVariable crn: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "createdAt.desc") sortBy: String,
    ) = documentsService.getDocuments(crn, PageRequest.of(page, size, sort(sortBy)), sortBy)

    private fun sort(sortString: String): Sort {
        val regex = Regex(pattern = "[A-Z]+\\.(ASC|DESC)", options = setOf(RegexOption.IGNORE_CASE))
        if (!regex.matches(sortString)) {
            throw InvalidRequestException("Sort criteria invalid format")
        }
        val sortBy = sortString.split(".")[0]
        val direction = sortString.split(".")[1].uppercase()
        return Sort.by(Sort.Direction.valueOf(direction), sortBy)
    }
}
