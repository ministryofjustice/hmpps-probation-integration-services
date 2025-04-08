package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

object PersonGenerator {
    val DEFAULT = generate(crn = "A000001")

    fun generate(
        crn: String,
        forename: String = UUID.randomUUID().toString().substring(0, 15),
        surname: String = UUID.randomUUID().toString().substring(0, 15),
        dateOfBirth: LocalDate = LocalDate.now().minusYears(Random.nextInt(16, 76).toLong()),
        pnc: String? = null,
        cro: String? = null,
        id: Long? = IdGenerator.getAndIncrement()
    ) = Person(
        id = id,
        crn = crn,
        forename = forename,
        surname = surname,
        dateOfBirth = dateOfBirth,
        gender = if (Random.nextBoolean()) ReferenceDataGenerator.GENDER_MALE else ReferenceDataGenerator.GENDER_FEMALE,
        surnameSoundex = "surnameSoundex",
        firstNameSoundex = "firstNameSoundex",
        middleNameSoundex = null,
        pncNumber = pnc,
        croNumber = cro,
    )
}

object PersonManagerGenerator {

    val DEFAULT = generate(person = PersonGenerator.DEFAULT)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        person: Person,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.UNALLOCATED,
        staff: Staff = StaffGenerator.UNALLOCATED,
        allocationDate: LocalDateTime = LocalDateTime.of(1900, 1, 1, 0, 0),
        allocationReason: ReferenceData = ReferenceDataGenerator.INITIAL_ALLOCATION
    ) = PersonManager(
        id = id,
        person = person,
        provider = provider,
        staff = staff,
        staffEmployeeID = staff.id,
        team = team,
        trustProviderTeamId = team.id,
        allocationDate = allocationDate,
        allocationReason = allocationReason
    )
}

object EqualityGenerator {
    val DEFAULT = generate(1L, 1L, false)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        personId: Long = 1L,
        softDeleted: Boolean = false
    ) = Equality(
        id = id,
        personId = personId,
        softDeleted = softDeleted
    )
}