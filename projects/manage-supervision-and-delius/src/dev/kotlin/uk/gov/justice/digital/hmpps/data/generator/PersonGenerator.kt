package uk.gov.justice.digital.hmpps.data.generator


import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Provision
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.Exclusion
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.User
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate

object PersonGenerator {

    val OVERVIEW = generateOverview("X000004")

    fun generateOverview(
        crn: String,
        forename: String = "Forename",
        secondName: String? = "Middle1",
        thirdName: String? = "Middle2",
        surname: String = "Surname",
        emailAddress: String? = "testemail",
        mobileNumber: String? = "1234",
        telephoneNumber: String? = "4321",
        preferredName: String? = "Dee",
        dateOfBirth: LocalDate = LocalDate.now().minusYears(50),
        gender: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "M", "Male"),
        ethnicity: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "A1", "Asian or Asian British: Indian"),
        primaryLanguage: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "001", "English"),
        id: Long = IdGenerator.getAndIncrement(),
        disabilities: List<Disability> = listOf(
            Disability(IdGenerator.getAndIncrement(), id, ReferenceData(IdGenerator.getAndIncrement(), "D01", "Mental Illness")),
            Disability(IdGenerator.getAndIncrement(), id, ReferenceData(IdGenerator.getAndIncrement(), "D02", "Visual Impairment"))),
        personalCircumstances:  List<PersonalCircumstance> = listOf(
            PersonalCircumstance(IdGenerator.getAndIncrement(), id, ReferenceData(IdGenerator.getAndIncrement(), "E01", "Employment"), ReferenceData(IdGenerator.getAndIncrement(), "FT01", "Full-time employed (30 or more hours per week)")),
            PersonalCircumstance(IdGenerator.getAndIncrement(), id, ReferenceData(IdGenerator.getAndIncrement(), "A02", "Accommodation"), ReferenceData(IdGenerator.getAndIncrement(), "FM01", "Friends/Family (settled)"))),
        provisions:  List<Provision> = listOf(
            Provision(IdGenerator.getAndIncrement(), id, ReferenceData(IdGenerator.getAndIncrement(), "FF01", "Flex refreshment breaks")),
            Provision(IdGenerator.getAndIncrement(), id, ReferenceData(IdGenerator.getAndIncrement(), "CC02", "Colour/visibility marking"))),

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
        primaryLanguage = primaryLanguage,
        disabilities = disabilities,
        emailAddress = emailAddress,
        mobileNumber = mobileNumber,
        nextAppointment = emptyList(),
        personalCircumstances = personalCircumstances,
        provisions = provisions,
        telephoneNumber = telephoneNumber,
        preferredName = preferredName
    )

    fun generateUserAccess(
        crn: String = "X000000",
        restrictions: List<User> = emptyList(),
        exclusions: List<User> = emptyList(),
        id: Long = IdGenerator.getAndIncrement()
    ): uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.Person {
        val person = uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.Person(
            id = id,
            crn = crn,
            restrictions = emptyList(),
            exclusions = emptyList(),
            restrictionMessage = "Restriction message",
            exclusionMessage = "Exclusion message"
        )
        person.set(person::restrictions, restrictions.map { Restriction(IdGenerator.getAndIncrement(), person, it) })
        person.set(person::exclusions, exclusions.map { Exclusion(IdGenerator.getAndIncrement(), person, it) })
        return person
    }
}
