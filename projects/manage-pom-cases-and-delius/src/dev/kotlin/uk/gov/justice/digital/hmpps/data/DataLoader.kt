package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import java.time.LocalDate

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ProviderGenerator.DEFAULT_PROVIDER.institution!!)
        save(ProviderGenerator.DEFAULT_PROVIDER)
        save(ReferenceDataGenerator.KEY_DATE_TYPE_DATASET)
        save(ReferenceDataGenerator.POM_ALLOCATION_DATASET)
        saveAll(ReferenceDataGenerator.ALL)
        saveAll(
            listOf(
                RegistrationGenerator.TYPE_MAPPA,
                RegistrationGenerator.TYPE_OTH,
                RegistrationGenerator.TYPE_DASO
            )
        )
        saveAll(
            ContactType.Code.entries.map {
                ContactType(
                    it.value,
                    IdGenerator.getAndIncrement()
                )
            }
        )

        save(ProviderGenerator.DEFAULT_DISTRICT)
        saveAll(PersonManagerGenerator.ALL.map { it.team } + ProviderGenerator.POM_TEAM + ProviderGenerator.UNALLOCATED_TEAM)

        save(ProviderGenerator.generateStaff("Test", "Test", "Test"))

        val staffMap = (PersonManagerGenerator.ALL.map { it.staff } + ProviderGenerator.UNALLOCATED_STAFF)
            .map { save(it) }
            .associateBy { it.code }

        UserGenerator.DEFAULT_STAFF_USER = save(
            StaffUser(
                UserGenerator.DEFAULT_STAFF_USER.username,
                staffMap[ProviderGenerator.DEFAULT_STAFF.code],
                UserGenerator.DEFAULT_STAFF_USER.id
            )
        )

        saveAll(
            listOf(
                PersonGenerator.DEFAULT,
                PersonGenerator.HANDOVER,
                PersonGenerator.NO_MAPPA,
                PersonGenerator.CREATE_HANDOVER_AND_START,
                PersonGenerator.UPDATE_HANDOVER_AND_START,
                PersonGenerator.CREATE_SENTENCE_CHANGED,
                PersonGenerator.PERSON_NOT_FOUND,
                PersonGenerator.PERSON_MULTIPLE_CUSTODIAL
            )
        )
        saveAll(
            PersonManagerGenerator.ALL.map {
                PersonManagerGenerator.generate(
                    team = it.team,
                    staff = staffMap[it.staff.code]!!,
                    person = it.person,
                    active = it.active,
                    softDeleted = it.softDeleted
                )
            }
        )

        saveAll(CaseAllocationGenerator.ALL.map { it.event })
        saveAll(CaseAllocationGenerator.ALL.map { it.event.disposal })
        saveAll(CaseAllocationGenerator.ALL)
        saveAll(
            listOf(
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_MAPPA,
                    ReferenceDataGenerator.LEVEL_M2,
                    LocalDate.now().minusDays(3)
                ),
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_MAPPA,
                    ReferenceDataGenerator.LEVEL_M1,
                    LocalDate.now().minusDays(1),
                    softDeleted = true
                ),
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_MAPPA,
                    ReferenceDataGenerator.LEVEL_M3,
                    LocalDate.now().minusDays(2),
                    deRegistered = true
                ),
                RegistrationGenerator.generate(
                    RegistrationGenerator.TYPE_OTH,
                    ReferenceDataGenerator.LEVEL_M1,
                    LocalDate.now().minusDays(1)
                )
            )
        )

        val sentenceChangedHandoverEvent =
            save(EventGenerator.generateEvent(PersonGenerator.CREATE_SENTENCE_CHANGED.id))
        val sentenceChangedHandoverDisposal =
            save(EventGenerator.generateDisposal(sentenceChangedHandoverEvent))
        save(EventGenerator.generateCustody(sentenceChangedHandoverDisposal))

        val notFoundSentenceChangedHandoverEvent =
            save(EventGenerator.generateEvent(PersonGenerator.PERSON_NOT_FOUND.id))
        val notFoundSentenceChangedHandoverDisposal =
            save(EventGenerator.generateDisposal(notFoundSentenceChangedHandoverEvent))
        save(EventGenerator.generateCustody(notFoundSentenceChangedHandoverDisposal))

        //Multiple custodial
        val multipleHandoverEvent1 =
            save(EventGenerator.generateEvent(PersonGenerator.PERSON_MULTIPLE_CUSTODIAL.id))
        val multipleHandoverEvent2 =
            save(EventGenerator.generateEvent(PersonGenerator.PERSON_MULTIPLE_CUSTODIAL.id))
        val multipleHandoverDisposal1 =
            save(EventGenerator.generateDisposal(multipleHandoverEvent1))
        val multipleHandoverDisposal2 =
            save(EventGenerator.generateDisposal(multipleHandoverEvent2))
        save(EventGenerator.generateCustody(multipleHandoverDisposal1))
        save(EventGenerator.generateCustody(multipleHandoverDisposal2))

        val handoverEvent = save(EventGenerator.generateEvent(PersonGenerator.HANDOVER.id))
        val handoverDisposal = save(EventGenerator.generateDisposal(handoverEvent))
        save(EventGenerator.generateCustody(handoverDisposal))

        val bothEvent = save(EventGenerator.generateEvent(PersonGenerator.CREATE_HANDOVER_AND_START.id))
        val bothDisposal = save(EventGenerator.generateDisposal(bothEvent))
        save(EventGenerator.generateCustody(bothDisposal))

        val handoverStartEvent =
            save(EventGenerator.generateEvent(PersonGenerator.UPDATE_HANDOVER_AND_START.id))
        val handoverStartDisposal = save(EventGenerator.generateDisposal(handoverStartEvent))
        val handoverStartCustody = save(EventGenerator.generateCustody(handoverStartDisposal))
        saveAll(
            listOf(
                EventGenerator.generateKeyDate(
                    handoverStartCustody,
                    ReferenceDataGenerator.KEY_DATE_HANDOVER_TYPE,
                    LocalDate.of(2023, 5, 2)
                ),
                EventGenerator.generateKeyDate(
                    handoverStartCustody,
                    ReferenceDataGenerator.KEY_DATE_HANDOVER_START_DATE_TYPE,
                    LocalDate.of(2023, 5, 1)
                )
            )
        )
    }
}
