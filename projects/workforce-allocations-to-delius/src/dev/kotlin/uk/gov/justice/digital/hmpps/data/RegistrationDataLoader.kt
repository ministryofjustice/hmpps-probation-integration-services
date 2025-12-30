package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegisterTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class RegistrationDataLoader(private val dataManager: DataManager) {
    fun loadData() {
        dataManager.saveAll(
            RegisterTypeGenerator.REGISTER_TYPES.map { it.flag }
        )
        dataManager.saveAll(RegisterTypeGenerator.REGISTER_TYPES)
        dataManager.saveAll(RegistrationGenerator.generateRegistrations())
        dataManager.save(PersonGenerator.NO_REGISTRATIONS)
    }
}