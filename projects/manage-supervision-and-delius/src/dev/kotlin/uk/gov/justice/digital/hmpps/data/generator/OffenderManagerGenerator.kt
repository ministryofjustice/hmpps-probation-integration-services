package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.ProbationAreaUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.ProbationAreaUserId
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object OffenderManagerGenerator {

    val BOROUGH = Borough("LTS_ALL", "Leicestershire All", IdGenerator.getAndIncrement())
    val DISTRICT = District("LTS_ALL", "Leicestershire All", BOROUGH, IdGenerator.getAndIncrement())
    val TEAM = Team(
        IdGenerator.getAndIncrement(),
        DISTRICT,
        DEFAULT_PROVIDER,
        "N07T02",
        "OMU B",
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(1)
    )
    val TEAM_1 = Team(
        IdGenerator.getAndIncrement(),
        DISTRICT,
        DEFAULT_PROVIDER,
        "T1",
        "team1",
        startDate = LocalDate.now().minusDays(3),
        endDate = LocalDate.now().minusDays(1)
    )
    val TEAM_2 = Team(
        IdGenerator.getAndIncrement(),
        DISTRICT,
        DEFAULT_PROVIDER,
        "T2",
        "team2",
        startDate = LocalDate.now().plusDays(1),
    )
    val STAFF_ROLE = ReferenceData(IdGenerator.getAndIncrement(), "PS1", "PS Other")
    val STAFF_1 = Staff(
        IdGenerator.getAndIncrement(),
        "Peter",
        "Parker",
        DEFAULT_PROVIDER,
        role = STAFF_ROLE,
        startDate = LocalDate.now())
    val STAFF_2 = Staff(
        IdGenerator.getAndIncrement(),
        "Bruce",
        "Wayne",
        DEFAULT_PROVIDER,
        role = STAFF_ROLE,
        startDate = LocalDate.now().minusDays(1)
    )
    val STAFF_3 = Staff(
        IdGenerator.getAndIncrement(),
        "Clark",
        "Kent",
        DEFAULT_PROVIDER,
        startDate = LocalDate.now().minusDays(2)
    )


    val STAFF_USER_1 = StaffUser(IdGenerator.getAndIncrement(), STAFF_1, "peter-parker", "peter", surname = "parker")
    val STAFF_USER_2 = StaffUser(IdGenerator.getAndIncrement(), STAFF_2, "bwayne", "bruce", surname = "wayne")
    val STAFF_USER_3 = StaffUser(IdGenerator.getAndIncrement(), STAFF_3, "ckent", "clark", surname = "kent")
    val STAFF_TEAM = ContactStaffTeam(StaffTeamLinkId(STAFF_1.id, TEAM))

    val PAU_USER_RECORD1 = ProbationAreaUser(ProbationAreaUserId(STAFF_USER_1, DEFAULT_PROVIDER))
    val PROVIDER_2 = generateProvider("W01", selectable = true)
    val PROVIDER_3 = generateProvider("A01", selectable = false)
    val PAU_USER_RECORD2 = ProbationAreaUser(ProbationAreaUserId(STAFF_USER_1, PROVIDER_2))
    val PAU_USER_RECORD3 = ProbationAreaUser(ProbationAreaUserId(STAFF_USER_1, PROVIDER_3))
    val PAU_USER_RECORD4 = ProbationAreaUser(ProbationAreaUserId(STAFF_USER_2, PROVIDER_2))

    val DEFAULT_LOCATION =
        Location(
            IdGenerator.getAndIncrement(),
            "B20_MTH",
            "1 Birmingham Street",
            "Bham House",
            "1",
            "Birmingham Street",
            "Birmingham",
            "West Midlands",
            "B20 3BA",
            startDate = LocalDate.now().minusDays(1)
        )

    val TEAM_OFFICE = TeamOfficeLink(TeamOfficeLinkId(TEAM.id, DEFAULT_LOCATION))
    val TEAM_OFFICE_1 = TeamOfficeLink(TeamOfficeLinkId(TEAM_1.id, DEFAULT_LOCATION))

    val OFFENDER_MANAGER_ACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            DEFAULT_PROVIDER,
            TEAM,
            STAFF_1,
            LocalDate.of(2025, 2, 10),
            lastUpdated = ZonedDateTime.of(LocalDate.of(2025, 2, 10), LocalTime.NOON, EuropeLondon)
        )

    val OFFENDER_MANAGER_INACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            DEFAULT_PROVIDER,
            TEAM,
            STAFF_2,
            LocalDate.of(2025, 2, 9),
            LocalDate.of(2025, 2, 10),
            lastUpdated = ZonedDateTime.of(LocalDate.of(2025, 2, 9), LocalTime.NOON, EuropeLondon),
            active = false
        )

    val PRISON_OFFENDER_MANAGER_ACTIVE = PrisonManager(
        IdGenerator.getAndIncrement(),
        PersonGenerator.OVERVIEW,
        DEFAULT_PROVIDER,
        TEAM,
        STAFF_3,
        LocalDate.of(2025, 2, 7),
        lastUpdated = ZonedDateTime.of(LocalDate.of(2025, 2, 7), LocalTime.NOON, EuropeLondon),
        active = true,
        softDeleted = false
    )

    val PRISON_OFFENDER_MANAGER_INACTIVE = PrisonManager(
        IdGenerator.getAndIncrement(),
        PersonGenerator.OVERVIEW,
        DEFAULT_PROVIDER,
        TEAM,
        STAFF_3,
        LocalDate.of(2025, 2, 7),
        lastUpdated = ZonedDateTime.of(LocalDate.of(2025, 2, 7), LocalTime.NOON, EuropeLondon),
        active = false,
        softDeleted = true
    )

    val RESPONSIBLE_OFFICER = ResponsibleOfficer(
        IdGenerator.getAndIncrement(),
        PersonGenerator.OVERVIEW.id,
        PRISON_OFFENDER_MANAGER_ACTIVE,
        ZonedDateTime.now(),
    )

    val RESPONSIBLE_OFFICER_OM_ACTIVE = ResponsibleOfficer(
        IdGenerator.getAndIncrement(),
        PersonGenerator.OVERVIEW.id,
        startDate = ZonedDateTime.now(),
        endDate = ZonedDateTime.now(),
        offenderManagerId = OFFENDER_MANAGER_ACTIVE.id
    )

    val RESPONSIBLE_OFFICER_OM_INACTIVE = ResponsibleOfficer(
        IdGenerator.getAndIncrement(),
        PersonGenerator.OVERVIEW.id,
        startDate = ZonedDateTime.now(),
        endDate = ZonedDateTime.now(),
        offenderManagerId = OFFENDER_MANAGER_INACTIVE.id
    )
}