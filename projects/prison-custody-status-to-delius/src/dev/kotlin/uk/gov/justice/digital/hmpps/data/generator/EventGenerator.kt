package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

object EventGenerator {

    fun nonCustodialEvent(
        person: Person,
        lengthInDays: Long = 365,
        disposalDate: ZonedDateTime = ZonedDateTime.of(2022, 5, 1, 0, 0, 0, 0, EuropeLondon),
        disposalCode: String = "DEF"
    ): Event {
        val event = Event(
            id = IdGenerator.getAndIncrement(),
            person = person
        )
        val disposal = Disposal(
            id = IdGenerator.getAndIncrement(),
            type = DisposalType(IdGenerator.getAndIncrement(), disposalCode, "NC"),
            date = disposalDate,
            lengthInDays = lengthInDays,
            event = event
        )
        event.disposal = disposal
        return event
    }

    fun custodialEvent(
        person: Person,
        institution: Institution?,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.IN_CUSTODY,
        disposalDate: ZonedDateTime = ZonedDateTime.of(2022, 5, 1, 0, 0, 0, 0, EuropeLondon),
        lengthInDays: Long = 365,
        disposalCode: String = "DEF"
    ): Event {
        val event = nonCustodialEvent(person, lengthInDays, disposalDate, disposalCode)
        val disposal = event.disposal!!
        val custody = Custody(
            id = IdGenerator.getAndIncrement(),
            status = ReferenceDataGenerator.CUSTODIAL_STATUS[custodialStatusCode]!!,
            institution = institution,
            disposal = disposal,
            statusChangeDate = ZonedDateTime.now().minusDays(1),
            locationChangeDate = ZonedDateTime.now().minusDays(1)
        )
        disposal.custody = custody
        return event
    }

    fun previouslyReleasedEvent(
        person: Person,
        institution: Institution?,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.RELEASED_ON_LICENCE,
        lengthInDays: Long = 365,
        releaseDate: ZonedDateTime = ZonedDateTime.now().minusMonths(6),
        releaseType: ReleaseTypeCode = ReleaseTypeCode.ADULT_LICENCE
    ): Event {
        val event = custodialEvent(person, institution, custodialStatusCode, lengthInDays = lengthInDays)
        val custody = event.disposal!!.custody!!
        val release = Release(
            date = releaseDate,
            type = ReferenceDataGenerator.RELEASE_TYPE[releaseType]!!,
            custody = custody,
            person = person,
            institutionId = institution?.id
        )
        custody.releases.add(release)
        event.firstReleaseDate = release.date
        return event
    }

    fun previouslyRecalledEvent(
        person: Person,
        institution: Institution?,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.RECALLED,
        recallDate: ZonedDateTime = ZonedDateTime.now().minusWeeks(1),
        releaseDate: ZonedDateTime = recallDate.minusMonths(6)
    ): Event {
        val event = previouslyReleasedEvent(person, institution, custodialStatusCode, releaseDate = releaseDate)
        val release = event.disposal!!.custody!!.releases[0]
        val recall = Recall(
            date = recallDate,
            reason = ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]!!,
            release = release,
            person = person
        )
        release.recall = recall
        return event
    }
}

fun Event.withManager(): Event {
    val om = OrderManagerGenerator.generate(this)
    this.set(Event::managers, listOf(om))
    return this
}
