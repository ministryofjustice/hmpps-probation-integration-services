package uk.gov.justice.digital.hmpps.integrations.delius.probationarea

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ProbationAreaRepository : JpaRepository<ProbationArea, Long> {
    fun findByCode(code: String): ProbationArea?
}

fun ProbationAreaRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Probation Area", "code", code)
