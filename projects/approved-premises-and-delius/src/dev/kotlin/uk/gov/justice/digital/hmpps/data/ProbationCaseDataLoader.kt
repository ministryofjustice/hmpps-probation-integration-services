package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.EXCLUDED_CASE
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator.RESTRICTED_CASE
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance.entity.PersonalCircumstanceType
import java.time.LocalDate

@Component
class ProbationCaseDataLoader(private val dataManager: DataManager) {
    fun loadData() {
        dataManager.saveAll(listOf(OffenceGenerator.OFFENCE_ONE, OffenceGenerator.OFFENCE_TWO))
        dataManager.save(ProbationCaseGenerator.COM_PROVIDER)
        dataManager.save(ProbationCaseGenerator.COM_LDU)
        dataManager.save(ProbationCaseGenerator.COM_TEAM.asTeam())
        dataManager.save(ProbationCaseGenerator.COM_UNALLOCATED)
        dataManager.save(ProbationCaseGenerator.CASE_COMPLEX)
        dataManager.save(ProbationCaseGenerator.CASE_SIMPLE)
        dataManager.save(ProbationCaseGenerator.CASE_X320741)
        dataManager.save(ProbationCaseGenerator.CASE_X320742)
        dataManager.save(ProbationCaseGenerator.CASE_X823998)
        dataManager.save(ProbationCaseGenerator.CASE_X698234)
        dataManager.save(ProbationCaseGenerator.CASE_X320811)
        dataManager.save(ProbationCaseGenerator.CASE_LAO_EXCLUSION)
        dataManager.save(ProbationCaseGenerator.CASE_LAO_RESTRICTED)

        dataManager.saveAll(
            listOf(
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_COMPLEX).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_SIMPLE).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X320741).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X320742).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X823998).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X698234).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_X320811).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_LAO_EXCLUSION).asPersonManager(),
                ProbationCaseGenerator.generateManager(ProbationCaseGenerator.CASE_LAO_RESTRICTED).asPersonManager()
            )
        )

        dataManager.save(
            PersonGenerator.generateRegistration(
                ProbationCaseGenerator.CASE_COMPLEX.asPerson(),
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.MAPPA.value]!!,
                LocalDate.now().minusDays(7),
                ReferenceDataGenerator.NON_MAPPA_CATEGORY,
                ReferenceDataGenerator.REGISTER_LEVELS["M2"]
            )
        )

        dataManager.save(
            PersonGenerator.generateRegistration(
                ProbationCaseGenerator.CASE_COMPLEX.asPerson(),
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.MAPPA.value]!!,
                LocalDate.now().minusDays(7),
                ReferenceDataGenerator.REGISTER_CATEGORIES["M3"],
                ReferenceDataGenerator.REGISTER_LEVELS["M2"]
            )
        )

        dataManager.save(
            PersonGenerator.generateRegistration(
                ProbationCaseGenerator.CASE_COMPLEX.asPerson(),
                ReferenceDataGenerator.REGISTER_TYPES[RegisterType.Code.SEX_OFFENCE.value]!!,
                LocalDate.now().minusDays(7)
            )
        )

        generateEventAndAddOffences(
            ProbationCaseGenerator.CASE_COMPLEX,
            eventId = 100001L,
            mainOffence = Pair(200001L, LocalDate.parse("2024-10-11")),
            additionalOffence = Pair(300001L, LocalDate.parse("2024-10-21"))
        )
        generateEventAndAddOffences(
            ProbationCaseGenerator.CASE_X320741,
            eventId = 100002L,
            mainOffence = Pair(200002L, LocalDate.parse("2024-10-12")),
            additionalOffence = Pair(300002L, LocalDate.parse("2024-10-22"))
        )
        generateEventAndAddOffences(
            ProbationCaseGenerator.CASE_LAO_RESTRICTED,
            eventId = 100003L,
            mainOffence = Pair(200003L, LocalDate.parse("2024-10-13")),
            additionalOffence = Pair(300003L, LocalDate.parse("2024-10-23"))
        )
        generateEventAndAddOffences(
            ProbationCaseGenerator.CASE_LAO_EXCLUSION,
            eventId = 100004L,
            mainOffence = Pair(200004L, LocalDate.parse("2024-10-14")),
            additionalOffence = Pair(300004L, LocalDate.parse("2024-10-24"))
        )
        generateEventAndAddOffences(
            ProbationCaseGenerator.CASE_X320742,
            eventId = 100005L,
            mainOffence = Pair(200002L, LocalDate.parse("2024-10-12")),
            additionalOffence = Pair(300002L, LocalDate.parse("2024-10-22"))
        )
        generateEventAndAddOffences(
            ProbationCaseGenerator.CASE_X823998,
            eventId = 100005L,
            mainOffence = Pair(200002L, LocalDate.parse("2024-10-12")),
            additionalOffence = Pair(300002L, LocalDate.parse("2024-10-22"))
        )
        generateEventAndAddOffences(
            ProbationCaseGenerator.CASE_X698234,
            eventId = 100005L,
            mainOffence = Pair(200002L, LocalDate.parse("2024-10-12")),
            additionalOffence = Pair(300002L, LocalDate.parse("2024-10-22"))
        )

        dataManager.saveAll(PersonalCircumstanceGenerator.PC_TYPES)
        dataManager.saveAll(PersonalCircumstanceGenerator.PC_SUB_TYPES)
        dataManager.save(
            PersonalCircumstanceGenerator.generate(
                ProbationCaseGenerator.CASE_COMPLEX.id,
                PersonalCircumstanceGenerator.PC_TYPES.first { it.code == PersonalCircumstanceType.Code.VETERAN.value },
                PersonalCircumstanceGenerator.PC_SUB_TYPES.first { it.description == PersonalCircumstanceType.Code.VETERAN.value + "SUB" }
            ))

        dataManager.save(RESTRICTED_CASE)
        dataManager.save(EXCLUDED_CASE)
        dataManager.save(LimitedAccessGenerator.generateRestriction(RESTRICTED_CASE.toLimitedAccessPerson()))
        dataManager.save(LimitedAccessGenerator.generateExclusion(EXCLUDED_CASE.toLimitedAccessPerson()))
    }

    private fun generateEventAndAddOffences(
        probationCase: ProbationCase,
        eventId: Long,
        mainOffence: Pair<Long, LocalDate>,
        additionalOffence: Pair<Long, LocalDate>,
    ) {
        val event = PersonGenerator.generateEvent(
            "1",
            probationCase.asPerson(),
            id = eventId
        ).apply(dataManager::save)

        dataManager.save(
            OffenceGenerator.generateMainOffence(
                event,
                OffenceGenerator.OFFENCE_ONE,
                id = mainOffence.first,
                date = mainOffence.second
            )
        )

        dataManager.save(
            OffenceGenerator.generateAdditionalOffence(
                event,
                OffenceGenerator.OFFENCE_TWO,
                id = additionalOffence.first,
                date = additionalOffence.second
            )
        )
    }
}

@Entity
@Table(name = "offender")
class MutableLimitedAccessPerson(
    @Column(columnDefinition = "char(7)")
    val crn: String,
    val exclusionMessage: String?,
    val restrictionMessage: String?,
    @Id
    @Column(name = "offender_id")
    val id: Long,
) {
    fun toLimitedAccessPerson() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
}
