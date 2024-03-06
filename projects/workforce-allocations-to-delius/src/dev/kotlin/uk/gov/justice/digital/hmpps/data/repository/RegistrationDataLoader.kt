package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.RegisterTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.registration.RegistrationRepository

@Component
@ConditionalOnProperty("seed.database")
class RegistrationDataLoader(
    private val referenceDataRepository: ReferenceDataRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val registrationRepository: RegistrationRepository
) {
    fun loadData() {
        referenceDataRepository.saveAll(
            RegisterTypeGenerator.REGISTER_TYPES.map { it.flag }
        )
        registerTypeRepository.saveAll(RegisterTypeGenerator.REGISTER_TYPES)
        registrationRepository.saveAll(RegistrationGenerator.generateRegistrations())
    }
}