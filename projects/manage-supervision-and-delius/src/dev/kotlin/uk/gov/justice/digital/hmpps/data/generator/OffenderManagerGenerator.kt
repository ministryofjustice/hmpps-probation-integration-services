package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object OffenderManagerGenerator {

    val BOROUGH = Borough("LTS_ALL", "Leicestershire All", IdGenerator.getAndIncrement())
    val DISTRICT = District("LTS_ALL", "Leicestershire All", BOROUGH, IdGenerator.getAndIncrement())
    val TEAM = Team(IdGenerator.getAndIncrement(), DISTRICT, DEFAULT_PROVIDER, "N07T02", "OMU B")

    val STAFF_1 = Staff(IdGenerator.getAndIncrement(), "Peter", "Parker", DEFAULT_PROVIDER, null)
    val STAFF_2 = Staff(IdGenerator.getAndIncrement(), "Bruce", "Wayne", DEFAULT_PROVIDER, null)
    val STAFF_3 = Staff(IdGenerator.getAndIncrement(), "Clark", "Kent", DEFAULT_PROVIDER, null)
    val STAFF_USER_1 = StaffUser(IdGenerator.getAndIncrement(), STAFF_1, "peter-parker", "peter", surname = "parker")
    val STAFF_USER_2 = StaffUser(IdGenerator.getAndIncrement(), STAFF_2, "bwayne", "bruce", surname = "wayne")
    val STAFF_USER_3 = StaffUser(IdGenerator.getAndIncrement(), STAFF_3, "ckent", "clark", surname = "kent")
    val STAFF_TEAM = ContactStaffTeam(StaffTeamLinkId(STAFF_1.id, TEAM))

    val DEFAULT_LOCATION =
        Location(
            IdGenerator.getAndIncrement(),
            "B20",
            "1 Birmingham Street",
            "Bham House",
            "1",
            "Birmingham Street",
            "Birmingham",
            "West Midlands",
            "B20 3BA"
        )

    val TEAM_OFFICE = TeamOfficeLink(TeamOfficeLinkId(TEAM.id, DEFAULT_LOCATION))

    val OFFENDER_MANAGER_ACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            ContactGenerator.DEFAULT_PROVIDER,
            TEAM,
            STAFF_1,
            LocalDate.of(2025, 2, 10),
            lastUpdated = ZonedDateTime.of(LocalDate.of(2025, 2, 10), LocalTime.NOON, EuropeLondon)
        )

    val OFFENDER_MANAGER_INACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            ContactGenerator.DEFAULT_PROVIDER,
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
        ContactGenerator.DEFAULT_PROVIDER,
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
        ContactGenerator.DEFAULT_PROVIDER,
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