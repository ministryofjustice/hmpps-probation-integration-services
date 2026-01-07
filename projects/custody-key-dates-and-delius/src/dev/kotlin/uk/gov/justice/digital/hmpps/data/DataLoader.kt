package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.DEFAULT_CUSTODY
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateCustodialSentence
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateEvent
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateOrderManager
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            listOf(
                ReferenceDataGenerator.DS_CUSTODY_STATUS,
                ReferenceDataGenerator.DS_KEY_DATE_TYPE
            )
        )
        save(ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS)
        val keyDateTypes = saveAll(ReferenceDataGenerator.KEY_DATE_TYPES.values)
        save(ContactTypeGenerator.EDSS)
        save(SentenceGenerator.DEFAULT_DISPOSAL_TYPE)

        save(PersonGenerator.DEFAULT)

        val event = save(generateEvent(PersonGenerator.DEFAULT))
        save(generateOrderManager(event))
        val disposal = save(generateDisposal(event))
        DEFAULT_CUSTODY = save(
            generateCustodialSentence(
                ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS,
                disposal,
                "38339A"
            )
        )

        saveAll(
            listOf(
                KeyDateGenerator.generate(
                    DEFAULT_CUSTODY,
                    keyDateTypes[2], //PED
                    LocalDate.parse("2022-10-26"),
                    false
                ),
                KeyDateGenerator.generate(
                    DEFAULT_CUSTODY,
                    keyDateTypes[0], //LED
                    LocalDate.parse("2024-09-10"),
                    false
                ),
                KeyDateGenerator.generate(
                    DEFAULT_CUSTODY,
                    keyDateTypes[3], //["SED"]!!,
                    LocalDate.parse("2024-08-10"),
                    false
                )
            )
        )
        createPersonWithKeyDates(PersonGenerator.DEFAULT, "58340A", keyDateTypes)

        createPersonWithKeyDates(PersonGenerator.PERSON_WITH_KEYDATES, "38340A", keyDateTypes)

        createPersonWithKeyDates(PersonGenerator.PERSON_WITH_KEYDATES_BY_CRN, "48340A", keyDateTypes)
    }

    private fun createPersonWithKeyDates(
        personRef: Person,
        bookingRef: String,
        keyDateTypes: List<ReferenceData>
    ): Custody {
        val person = save(personRef)
        val event = save(generateEvent(person, "1"))
        save(generateOrderManager(event))
        val disposal = save(generateDisposal(event))
        val custody = save(
            generateCustodialSentence(
                ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS,
                disposal,
                bookingRef
            )
        )
        saveAll(keyDateTypes.flatMap { referenceData ->
            when (referenceData.code) {
                "LED" -> listOf(
                    KeyDate(custody, referenceData, LocalDate.parse("2025-09-11")).also { it.softDeleted = true }
                )

                "SED" -> listOf(
                    KeyDate(custody, referenceData, LocalDate.parse("2025-09-11")),
                    KeyDate(custody, referenceData, LocalDate.parse("2025-09-10")).also { it.softDeleted = true }
                )

                else -> listOf(KeyDate(custody, referenceData, LocalDate.parse("2025-12-11")))
            }
        })
        return custody
    }
}
