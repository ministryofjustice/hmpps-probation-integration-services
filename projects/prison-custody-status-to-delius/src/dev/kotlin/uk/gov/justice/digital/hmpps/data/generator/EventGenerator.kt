package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.recall.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.Release
import java.time.ZonedDateTime

object EventGenerator {
    fun unSentencedEvent(person: Person) = Event(
        id = IdGenerator.getAndIncrement(),
        person = person,
    )

    fun nonCustodialEvent(person: Person): Event {
        val event = Event(
            id = IdGenerator.getAndIncrement(),
            person = person,
        )
        val disposal = Disposal(
            id = IdGenerator.getAndIncrement(),
            type = DisposalType(IdGenerator.getAndIncrement(), "NC"),
            date = ZonedDateTime.of(2022, 5, 1, 0, 0, 0, 0, EuropeLondon), // must be before release date in message
            event = event,
        )
        event.disposal = disposal
        return event
    }

    fun custodialEvent(
        person: Person,
        institution: Institution,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.IN_CUSTODY,
    ): Event {
        val event = nonCustodialEvent(person)
        val disposal = event.disposal!!
        val custody = Custody(
            id = IdGenerator.getAndIncrement(),
            status = ReferenceDataGenerator.CUSTODIAL_STATUS[custodialStatusCode]!!,
            institution = institution,
            disposal = disposal,
            statusChangeDate = ZonedDateTime.now().minusDays(1),
            locationChangeDate = ZonedDateTime.now().minusDays(1),
        )
        disposal.custody = custody
        return event
    }

    fun previouslyReleasedEvent(
        person: Person,
        institution: Institution,
        releaseDate: ZonedDateTime = ZonedDateTime.now().minusMonths(6),
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.IN_CUSTODY,
    ): Event {
        val event = custodialEvent(person, institution, custodialStatusCode)
        val custody = event.disposal!!.custody!!
        val release = Release(
            date = releaseDate,
            type = ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE]!!,
            custody = custody,
            person = person,
        )
        custody.releases.add(release)
        event.firstReleaseDate = release.date
        return event
    }

    fun previouslyRecalledEvent(
        person: Person,
        institution: Institution,
        recallDate: ZonedDateTime,
        releaseDate: ZonedDateTime = recallDate.minusMonths(6),
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.IN_CUSTODY,
    ): Event {
        val event = previouslyReleasedEvent(person, institution, releaseDate, custodialStatusCode)
        val release = event.disposal!!.custody!!.releases[0]
        val recall = Recall(
            date = recallDate,
            release = release
        )
        release.recall = recall
        return event
    }
}
