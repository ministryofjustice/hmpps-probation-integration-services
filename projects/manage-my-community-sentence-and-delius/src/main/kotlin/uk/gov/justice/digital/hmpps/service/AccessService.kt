package uk.gov.justice.digital.hmpps.service

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.repository.AccessRepository

@Service
class AccessService(private val accessRepository: AccessRepository) {
    fun checkAccess(crn: String) {
        if (!accessRepository.existsByCrn(crn)) throw NotFoundException("Person", "CRN", crn)
        if (!accessRepository.isAllowed(crn)) throw AccessDeniedException("$crn does not meet the eligibility criteria")
    }
}
