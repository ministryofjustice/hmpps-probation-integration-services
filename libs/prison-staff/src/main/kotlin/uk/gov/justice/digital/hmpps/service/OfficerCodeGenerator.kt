package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.repository.PrisonStaffRepository

@Component
class OfficerCodeGenerator(private val staffRepository: PrisonStaffRepository) {
    fun generateFor(probationAreaCode: String): String {
        val prefix = probationAreaCode.substring(0, 3)
        return staffRepository.getNextStaffReference(prefix) ?: "${prefix}A000"
    }
}
