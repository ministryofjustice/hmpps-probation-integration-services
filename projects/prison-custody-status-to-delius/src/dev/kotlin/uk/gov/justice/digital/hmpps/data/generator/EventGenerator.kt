package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.recall.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.Release
import java.time.ZonedDateTime

object EventGenerator {
    fun unSentencedEvent(person: Person) = Event(
        id = IdGenerator.getAndIncrement(),
        person = person
    )

    fun nonCustodialEvent(person: Person, lengthInDays: Long = 365): Event {
        val event = Event(
            id = IdGenerator.getAndIncrement(),
            person = person
        )
        val disposal = Disposal(
            id = IdGenerator.getAndIncrement(),
            type = DisposalType(IdGenerator.getAndIncrement(), "NC"),
            date = ZonedDateTime.of(2022, 5, 1, 0, 0, 0, 0, EuropeLondon), // must be before release date in message
            lengthInDays = lengthInDays,
            event = event
        )
        event.disposal = disposal
        return event
    }

    fun custodialEvent(
        person: Person,
        institution: Institution,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.IN_CUSTODY,
        lengthInDays: Long = 365
    ): Event {
        val event = nonCustodialEvent(person, lengthInDays)
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
        institution: Institution,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.RELEASED_ON_LICENCE,
        lengthInDays: Long = 365,
        releaseDate: ZonedDateTime = ZonedDateTime.now().minusMonths(6),
        releaseType: ReleaseTypeCode = ReleaseTypeCode.ADULT_LICENCE
    ): Event {
        val event = custodialEvent(person, institution, custodialStatusCode, lengthInDays)
        val custody = event.disposal!!.custody!!
        val release = Release(
            date = releaseDate,
            type = ReferenceDataGenerator.RELEASE_TYPE[releaseType]!!,
            custody = custody,
            person = person,
            institutionId = institution.id,
            length = null
        )
        custody.releases.add(release)
        event.firstReleaseDate = release.date
        return event
    }

    fun previouslyRecalledEvent(
        person: Person,
        institution: Institution,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.RECALLED,
        recallDate: ZonedDateTime = ZonedDateTime.now().minusWeeks(1),
        releaseDate: ZonedDateTime = recallDate.minusMonths(6)
    ): Event {
        val event = previouslyReleasedEvent(person, institution, custodialStatusCode, releaseDate = releaseDate)
        val release = event.disposal!!.custody!!.releases[0]
        val recall = Recall(
            date = recallDate,
            reason = ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]!!,
            release = release,
            person = person
        )
        release.recall = recall
        return event
    }
}
