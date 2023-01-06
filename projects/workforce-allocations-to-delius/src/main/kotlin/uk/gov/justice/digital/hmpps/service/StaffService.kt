package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.OfficerView
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import java.time.LocalDate

@Service
class StaffService(
    private val staffRepository: StaffRepository,
) {
    fun getOfficerView(code: String): OfficerView {
        val staff = staffRepository.findByCode(code) ?: throw NotFoundException("Staff", "code", code)
        return OfficerView(
            code,
            Name(staff.forename, staff.middleName, staff.surname),
            staff.grade?.description,
            "email",
            staffRepository.getSentencesDueCountByStaffId(staff.id, LocalDate.now().minusWeeks(4)),
            staffRepository.getKeyDateCountByCodeAndStaffId(staff.id, "EXP", LocalDate.now().minusWeeks(4)),
            staffRepository.getParoleReportsDueCountByStaffId(staff.id, LocalDate.now().minusWeeks(4))
        )
    }
}
