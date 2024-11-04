package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.LocalDate

object OffenderManagerGenerator {

    val BOROUGH = Borough("LTS_ALL", "Leicestershire All", IdGenerator.getAndIncrement())
    val DISTRICT = District("LTS_ALL", "Leicestershire All", BOROUGH, IdGenerator.getAndIncrement())
    val TEAM = Team(IdGenerator.getAndIncrement(), DISTRICT, DEFAULT_PROVIDER,"N07T02", "OMU B")

    val STAFF_1 = Staff(IdGenerator.getAndIncrement(), "Peter", "Parker", DEFAULT_PROVIDER, null)
    val STAFF_2 = Staff(IdGenerator.getAndIncrement(), "Bruce", "Wayne", DEFAULT_PROVIDER, null)
    val STAFF_USER_1 = StaffUser(IdGenerator.getAndIncrement(), STAFF_1, "peter-parker")
    val STAFF_USER_2 = StaffUser(IdGenerator.getAndIncrement(), STAFF_2, "bwayne")

    val DEFAULT_LOCATION = Location(IdGenerator.getAndIncrement(), "B20", "1 Birmingham Street")

    val TEAM_OFFICE = TeamOfficeLink(TeamOfficeLinkId(TEAM.id, DEFAULT_LOCATION))

    val OFFENDER_MANAGER_ACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            ContactGenerator.DEFAULT_PROVIDER,
            TEAM,
            STAFF_1,
            null
        )
    val OFFENDER_MANAGER_INACTIVE =
        OffenderManager(
            IdGenerator.getAndIncrement(),
            PersonGenerator.OVERVIEW,
            ContactGenerator.DEFAULT_PROVIDER,
            TEAM,
            STAFF_2,
            LocalDate.now(),
            active = false
        )
}