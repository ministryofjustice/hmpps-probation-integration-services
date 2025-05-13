package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_1
import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object PersonManagerGenerator {

    val DEFAULT_PROVIDER = Provider(IdGenerator.getAndIncrement(), "A", "Default Test Provider", true)
    val PROVIDER_1 = Provider(IdGenerator.getAndIncrement(), "B", "Test Provider 1", true)
    val PROVIDER_2 = Provider(IdGenerator.getAndIncrement(), "C", "Test Provider 1", true)
    val DEFAULT_BOROUGH = Borough(IdGenerator.getAndIncrement(), "A", "Default Test PDU", DEFAULT_PROVIDER)
    val BOROUGH_1 = Borough(IdGenerator.getAndIncrement(), "B", "Test PDU 1", PROVIDER_1)
    val BOROUGH_2 = Borough(IdGenerator.getAndIncrement(), "C", "Test PDU 2", PROVIDER_2)
    val DEFAULT_DISTRICT = District(IdGenerator.getAndIncrement(), "Default Test Lau", DEFAULT_BOROUGH)
    val DISTRICT_1 = District(IdGenerator.getAndIncrement(), "Test Lau 1", BOROUGH_1)
    val DISTRICT_2 = District(IdGenerator.getAndIncrement(), "Test Lau 2", BOROUGH_2)
    val DEFAULT_TEAM =
        generateTeam(code = "D", description = "Team 1", district = DEFAULT_DISTRICT, email = "testing@test.none")
    val TEAM_1 = generateTeam(code = "E", description = "Team 1", district = DISTRICT_1)
    val TEAM_2 = generateTeam(code = "F", description = "Team 2", district = DISTRICT_2)
    val PERSON_MANAGER = generatePersonManager(PERSON_1)

    private fun generatePersonManager(person: Person) =
        PersonManager(IdGenerator.getAndIncrement(), person, DEFAULT_TEAM, false, true)

    private fun generateTeam(
        code: String,
        description: String,
        district: District,
        telephone: String? = null,
        email: String? = null,
        startDate: LocalDate = LocalDate.now().minusDays(10),
        endDate: LocalDate? = null
    ) =
        Team(
            IdGenerator.getAndIncrement(),
            code = code,
            description = description,
            telephone = telephone,
            emailAddress = email,
            addresses = emptyList(),
            startDate = startDate,
            endDate = endDate,
            district = district
        )
}
