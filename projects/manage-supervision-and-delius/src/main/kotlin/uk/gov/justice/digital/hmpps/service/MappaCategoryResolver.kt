package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RegistrationRepository

@Service
class MappaCategoryResolver(private val registrationRepository: RegistrationRepository) {
    fun resolveMappaCategory(offenderId: Long): Int {
        val registration = registrationRepository
            .findFirstByPersonIdAndTypeCodeOrderByIdDesc(
                offenderId,
                "MAPP"
            )

        return when (registration?.category?.code) {
            "M1" -> 1
            "M2" -> 2
            "M3" -> 3
            "M4" -> 4
            else -> 0
        }
    }
}

