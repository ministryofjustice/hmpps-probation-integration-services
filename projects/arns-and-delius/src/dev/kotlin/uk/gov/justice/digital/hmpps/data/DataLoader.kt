package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.EXCLUDED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.FULL_ACCESS_USER
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.LIMITED_ACCESS_USER
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.RESTRICTED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.UNLIMITED_ACCESS
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.entity.AdditionalSentence
import java.time.LocalDate

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        setupReferenceData()
        setupLimitedAccessData()
        setupCaseData()
    }

    private fun setupReferenceData() {
        save(ReferenceDataGenerator.MALE)
        save(OffenceGenerator.DEFAULT)
        save(OffenceGenerator.SECOND_OFFENCE)
    }

    private fun setupLimitedAccessData() {
        save(FULL_ACCESS_USER)
        save(LIMITED_ACCESS_USER)
        save(UNLIMITED_ACCESS)
        save(EXCLUDED_CASE)
        save(RESTRICTED_CASE)
        save(LimitedAccessGenerator.generateExclusion(EXCLUDED_CASE))
        save(LimitedAccessGenerator.generateRestriction(RESTRICTED_CASE))
    }

    private fun setupCaseData() {
        save(PersonGenerator.NO_EVENTS_PERSON)
        val personWithOffences = PersonGenerator.PERSON_WITH_OFFENCES
        save(personWithOffences)
        val (activeEvent, inactiveEvent) = personWithOffences.events
        saveAll(
            AdditionalSentence(
                id = IdGenerator.getAndIncrement(),
                event = activeEvent,
                person = personWithOffences,
                startDate = LocalDate.of(2026, 3, 10),
                endDate = LocalDate.of(2026, 9, 10),
            ),
            AdditionalSentence(
                id = IdGenerator.getAndIncrement(),
                event = inactiveEvent,
                person = personWithOffences,
                startDate = LocalDate.of(2026, 4, 20),
                endDate = LocalDate.of(2026, 10, 20),
            ),
            AdditionalSentence(
                id = IdGenerator.getAndIncrement(),
                event = activeEvent,
                person = personWithOffences,
                startDate = LocalDate.of(2026, 3, 15),
                endDate = LocalDate.of(2026, 9, 15),
                softDeleted = true,
            ),
        )
    }
}
