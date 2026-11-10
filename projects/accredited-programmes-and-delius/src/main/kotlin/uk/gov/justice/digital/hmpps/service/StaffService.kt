package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.repository.StaffRepository

@Service
class StaffService(private val staffRepository: StaffRepository) {
    fun getStaffByCode(code: String): Name =
        staffRepository.findByCode(code)?.let { Name(it.forename, it.surname, it.middleName) }
            ?: throw NotFoundException("Staff", "code", code)
}
