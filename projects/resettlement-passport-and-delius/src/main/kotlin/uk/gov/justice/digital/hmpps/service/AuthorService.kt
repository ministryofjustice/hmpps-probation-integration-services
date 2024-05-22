package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Author
import uk.gov.justice.digital.hmpps.api.model.AuthorDetails
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.retry.retry

@Service
class AuthorService(
    private val staffService: StaffService,
    private val probationAreaRepository: ProbationAreaRepository,
    private val teamRepository: TeamRepository
) {
    fun authorDetails(author: Author): AuthorDetails {
        if (author.prisonCode.length < 3) throw InvalidRequestException("Prison Code", author.prisonCode)
        val probationArea = probationAreaRepository.findByInstitutionNomisCode(author.prisonCode.substring(0, 3))
            ?: throw InvalidRequestException(
                "Probation Area not found for NOMIS institution: ${author.prisonCode.substring(0, 3)}"
            )
        val team = teamRepository.findByCode("${probationArea.code}CSN")
            ?: throw InvalidRequestException("Team not found for prison code: ${author.prisonCode.substring(0, 3)}")
        val staff = getStaff(probationArea, team, author)
        return AuthorDetails(staff, team, probationArea)
    }

    private fun getStaff(probationArea: ProbationArea, team: Team, author: Author): Staff {
        val findStaff = { staffService.findStaff(probationArea.id, author) }
        return retry(3) {
            findStaff() ?: staffService.create(probationArea, team, author)
        }
    }
}
