package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateExclusion
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.generateRestriction
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class LimitedAccessDataLoader(private val dataManager: DataManager) {
    fun loadData() {
        dataManager.saveAll(listOf(UserGenerator.LIMITED_ACCESS_USER))
        dataManager.saveAll(
            listOf(
                PersonGenerator.EXCLUSION,
                PersonGenerator.RESTRICTION,
                PersonGenerator.RESTRICTION_EXCLUSION
            )
        )

        dataManager.save(LimitedAccessGenerator.EXCLUSION)
        dataManager.save(LimitedAccessGenerator.RESTRICTION)
        dataManager.save(generateExclusion(person = PersonGenerator.RESTRICTION_EXCLUSION))
        dataManager.save(generateRestriction(person = PersonGenerator.RESTRICTION_EXCLUSION))
    }
}
