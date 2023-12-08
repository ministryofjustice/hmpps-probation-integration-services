package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.ldap.core.LdapTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.entity.CommunityManager
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.model.CommunityManagerResponse
import uk.gov.justice.digital.hmpps.repository.CommunityManagerRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@RestController
class ApiController(
    private val personRepository: PersonRepository,
    private val communityManagerRepository: CommunityManagerRepository,
    private val ldapTemplate: LdapTemplate,
) {
    @PreAuthorize("hasRole('ROLE_PRISON_EDUCATION_AND_DELIUS')")
    @GetMapping(value = ["/probation-case/{prisonerId}/community-manager"])
    @Operation(
        summary = "Get the current active community manager for a probation case",
        description = """Accepts the prisoner identifier (NOMS number) and returns the 
            currently active community manager of the probation case.
            <p>Requires `ROLE_PRISON_EDUCATION_AND_DELIUS`.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "404",
                description = "No probation case found with the given prisoner identifier. This could mean the Prison and Probation cases have not been linked yet.",
                content = [Content(schema = Schema(implementation = ErrorResponse::class), examples = [ExampleObject("""{"status": 404, "message": "Person with prisonerId of A0000AA not found"}""")])],
            ),
        ],
    )
    fun getCommunityManager(
        @PathVariable prisonerId: String,
    ) =
        personRepository.findByPrisonerId(prisonerId)
            ?.let { communityManagerRepository.findByPersonId(it.id).response }
            ?: throw NotFoundException("Person", "prisonerId", prisonerId)

    val CommunityManager.response get() =
        CommunityManagerResponse(
            firstName = staff.forename,
            lastName = staff.surname,
            email = staff.user?.username?.let { ldapTemplate.findEmailByUsername(it) },
        )
}
