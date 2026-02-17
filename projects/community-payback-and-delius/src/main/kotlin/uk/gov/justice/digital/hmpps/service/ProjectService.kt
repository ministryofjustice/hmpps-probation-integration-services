package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProjectRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.Project

@Service
class ProjectService(
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository,
) {
    fun getProject(code: String) =
        unpaidWorkProjectRepository.findByCode(code)?.let { Project(it) }.orNotFoundBy("code", code)
}