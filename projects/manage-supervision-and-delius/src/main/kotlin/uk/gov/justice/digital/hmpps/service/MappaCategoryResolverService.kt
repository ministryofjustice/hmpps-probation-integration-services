package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RegistrationRepository

@Service
class MappaCategoryResolverService(private val registrationRepository: RegistrationRepository) {
    fun resolveMappaCategory(offenderId: Long): Int = registrationRepository.findByMappaCategoryByPersonId(offenderId) ?: 0
}

