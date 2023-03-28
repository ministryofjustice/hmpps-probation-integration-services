package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.District
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Team
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PROVIDER = Provider(IdGenerator.getAndIncrement(), "TST", "Provider description")
    val DEFAULT_LAU = District(IdGenerator.getAndIncrement(), "Local admin unit")
    val DEFAULT_TEAM = Team(IdGenerator.getAndIncrement(), "TEAM01", "Team description", district = DEFAULT_LAU)
    val DEFAULT_STAFF = Staff(IdGenerator.getAndIncrement(), "STAFF01", forename = "Forename", surname = "Surname")

    val RECOMMENDATION_STARTED = generate("X000001")
    val DECISION_TO_RECALL = generate("X000002")
    val DECISION_NOT_TO_RECALL = generate("X000003")
    val CASE_SUMMARY = generateCaseSummary("X000004")

    fun generate(
        crn: String,
        providerId: Long = DEFAULT_PROVIDER.id,
        teamId: Long = DEFAULT_TEAM.id,
        staffId: Long = DEFAULT_STAFF.id,
        id: Long = IdGenerator.getAndIncrement()
    ): Person {
        val person = Person(id, crn)
        val personManager = PersonManager(IdGenerator.getAndIncrement(), person, providerId, teamId, staffId)
        person.set("manager", personManager)
        return person
    }

    fun generateCaseSummary(
        crn: String,
        forename: String = "Forename",
        secondName: String? = "Middle1",
        thirdName: String? = "Middle2",
        surname: String = "Surname",
        dateOfBirth: LocalDate = LocalDate.now().minusYears(50),
        gender: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "M", "Male"),
        ethnicity: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "A1", "Asian or Asian British: Indian"),
        primaryLanguage: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "001", "English"),
        id: Long = IdGenerator.getAndIncrement()
    ) = uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person(
        id = id,
        crn = crn,
        forename = forename,
        secondName = secondName,
        thirdName = thirdName,
        surname = surname,
        nomsNumber = "TEST123",
        croNumber = "TEST",
        pncNumber = "TEST123456789",
        mostRecentPrisonerNumber = "TEST",
        dateOfBirth = dateOfBirth,
        gender = gender,
        ethnicity = ethnicity,
        primaryLanguage = primaryLanguage
    )
}
