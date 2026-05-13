package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Staff as OffenderManagerStaff
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Team as OffenderManagerTeam
import java.time.LocalDate
import java.time.ZonedDateTime

object UnallocatedAlertGenerator {

    val PROVIDER = generateProvider("UAL", selectable = true)
    val BOROUGH = generateBorough("UALB", provider = PROVIDER)
    val DISTRICT = generateDistrict("UALD", borough = BOROUGH)

    val STAFF = OffenderManagerStaff(
        id = IdGenerator.getAndIncrement(),
        code = "UALST1",
        forename = "Alert",
        surname = "Officer",
        provider = PROVIDER,
        startDate = LocalDate.now()
    )

    val STAFF_USER = StaffUser(
        id = IdGenerator.getAndIncrement(),
        staff = STAFF,
        username = "ual-alert-officer",
        forename = "Alert",
        surname = "Officer"
    )

    val TEAM = OffenderManagerTeam(
        id = IdGenerator.getAndIncrement(),
        district = DISTRICT,
        provider = PROVIDER,
        code = "UALTM1",
        description = "Unallocated Alert Team",
        startDate = LocalDate.now()
    )

    val PERSON = PersonGenerator.generateOverview("UAL0001", forename = "Alert", surname = "Person")

    val EVENT = PersonGenerator.generateEvent(
        PERSON,
        eventNumber = "1",
        notes = "",
        additionalOffences = emptyList()
    )

    val OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PERSON,
        provider = PROVIDER,
        team = TEAM,
        staff = STAFF,
        allocationDate = LocalDate.now(),
        lastUpdated = ZonedDateTime.now(EuropeLondon)
    )

    val RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        personId = PERSON.id,
        startDate = ZonedDateTime.now(EuropeLondon),
        endDate = null,
        offenderManagerId = OFFENDER_MANAGER.id
    )

    val ALERT_CONTACT = Contact(
        id = IdGenerator.getAndIncrement(),
        person = PERSON,
        type = ContactGenerator.BREACH_CONTACT_TYPE,
        date = ZonedDateTime.now(EuropeLondon).minusDays(1).toLocalDate(),
        alert = true,
        staff = null,
        description = "Alert with no staff",
        notes = null,
        event = EVENT,
    )

    val CONTACT_ALERT = ContactAlert(
        contact = ALERT_CONTACT,
        typeId = ContactGenerator.BREACH_CONTACT_TYPE.id,
        personId = PERSON.id,
        teamId = TEAM.id,
        personManagerId = OFFENDER_MANAGER.id,
        staff = STAFF,
        id = IdGenerator.getAndIncrement()
    )
}

