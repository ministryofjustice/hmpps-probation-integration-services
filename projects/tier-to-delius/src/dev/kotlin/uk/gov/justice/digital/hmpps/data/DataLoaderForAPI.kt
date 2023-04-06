package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.controller.entity.CaseEntityRepository
import uk.gov.justice.digital.hmpps.data.generator.CaseEntityGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository

@Component
@Profile("dev", "integration-test")
class DataLoaderForAPI(
    private val caseEntityRepository: CaseEntityRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val referenceDataSetRepository: ReferenceDataSetRepository,
) : ApplicationListener<ApplicationReadyEvent> {

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        referenceDataSetRepository.save(ReferenceDataSetGenerator.GENDER)
        referenceDataRepository.save(ReferenceDataGenerator.GENDER_MALE)
        referenceDataRepository.save(ReferenceDataGenerator.TIER_ONE)

        caseEntityRepository.save(CaseEntityGenerator.DEFAULT)

    }
}
