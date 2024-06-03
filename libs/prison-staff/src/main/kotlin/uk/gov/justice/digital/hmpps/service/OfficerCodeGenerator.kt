package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exceptions.StaffCodeExhaustedException
import uk.gov.justice.digital.hmpps.repository.PrisonStaffRepository

@Component
class OfficerCodeGenerator(private val staffRepository: PrisonStaffRepository) {
    private val alphabet = ('A'..'Z').toList()

    fun generateFor(probationAreaCode: String, index: Int = 0): String {
        if (index == alphabet.size) {
            throw StaffCodeExhaustedException(probationAreaCode)
        }
        val prefix = probationAreaCode.substring(0, 3) + alphabet[index]
        val latest = staffRepository.getLatestStaffReference("^$prefix\\d{3}$")
        val number = latest?.substring(latest.length - 3)?.toInt()?.plus(1) ?: 1
        return if (number > 999) {
            generateFor(probationAreaCode, index + 1)
        } else {
            val suffix = number.toString().padStart(3, '0')
            prefix + suffix
        }
    }
}
