package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.ldap.byUsername
import uk.gov.justice.digital.hmpps.model.AuthenticationRequest
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Validated
@RestController
@Tag(name = "Authentication")
class AuthenticationController(
    private val ldapTemplate: LdapTemplate,
    private val telemetryService: TelemetryService,
) {
    @PostMapping("/authenticate")
    @PreAuthorize("hasRole('PROBATION_API__HMPPS_AUTH__AUTHENTICATE')")
    @Operation(description = "Authenticate a Delius username and password. Requires `PROBATION_API__HMPPS_AUTH__AUTHENTICATE`.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User authenticated",
                content = [Content(mediaType = "text/plain")]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication failure",
                content = [Content(mediaType = "text/plain")]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Client role required: `DELIUS_USER_AUTH`",
                content = [Content(mediaType = "text/plain")]
            )
        ]
    )
    fun authenticate(
        @Valid @RequestBody
        request: AuthenticationRequest
    ) = try {
        ldapTemplate.authenticate(query().byUsername(request.username), request.password)
        ResponseEntity.ok().build()
    } catch (e: Exception) {
        telemetryService.trackException(e, mapOf("username" to request.username, "error" to "AuthenticationFailure"))
        ResponseEntity.status(401)
            .body(ErrorResponse(401, "Authentication failure"))
    }
}
