package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.UserCaseloadIndicator
import uk.gov.justice.digital.hmpps.service.entity.CaseloadRepository
import uk.gov.justice.digital.hmpps.service.entity.StaffRepository

@Service
class CaseloadService(
    private val staffRepository: StaffRepository,
    private val caseloadRepository: CaseloadRepository
) {
    fun isInCaseload(username: String, crn: String): UserCaseloadIndicator {
        val staff = staffRepository.findByUsername(username) ?: throw NotFoundException("Unable to find staff record")
        val count = caseloadRepository.countByStaffIdAndPersonCrn(staff.id, crn)
        return UserCaseloadIndicator(count > 0)
    }
}