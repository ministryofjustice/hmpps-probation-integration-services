package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.services.Identifier
import uk.gov.justice.digital.hmpps.services.ProbationRecordService

@RestController
class ProbationRecordResource(val prService: ProbationRecordService) {
    @PreAuthorize("hasRole('ROLE_MANAGE_POM_CASES')")
    @GetMapping(value = ["/case-records/{identifier}"])
    @Operation(
        summary = "Background information on the probation case",
        description = """<p>Background details of the probation case for the purposes
            of allocating a new Prison Offender Manager. The service will return
            case information for either the CRN or NOMS number supplied in the
            request.</p>
            <p>Requires `ROLE_MANAGE_POM_CASES`.</p>
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "404",
                description = """No probation case found with the given identifier.
                    For a prison identifier this could mean the Prison and Probation cases
                    have not been linked yet.""",
                content = [
                    Content(schema = Schema(implementation = ErrorResponse::class)),
                ],
            ),
        ],
    )
    fun handle(
        @PathVariable("identifier") identifier: String,
    ) = prService.findByIdentifier(Identifier(identifier))
}
