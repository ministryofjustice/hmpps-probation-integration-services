package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessment
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.LocalDate
import java.util.*

object AssessmentGenerator {
    fun generate(
        person: Person,
        contact: Contact,
        date: LocalDate = LocalDate.now().minusDays(7),
        eventNumber: String = "1",
        court: Court? = null,
        offence: Offence? = null,
        totalScore: Long = 50,
        description: String? = "Oasys Assessment",
        assessedBy: String? = "John Smith",
        riskFlags: String? = "M,N,M,L,N,L,L,H,N",
        concernFlags: String? = "YES,NO,NO,DK,YES,NO,NO,YES",
        dateCreated: LocalDate = date,
        dateReceived: LocalDate = LocalDate.now().minusDays(2),
        initialSentencePlanDate: LocalDate? = null,
        sentencePlanReviewDate: LocalDate? = null,
        reviewTerminated: Boolean? = null,
        reviewNumber: String? = null,
        layerType: String? = "Layer_3",
        ogrsScore1: Long? = 21,
        ogrsScore2: Long? = 42,
        ogpScore1: Long? = 13,
        ogpScore2: Long? = 26,
        ovpScore1: Long? = 6,
        ovpScore2: Long? = 12,
        softDeleted: Boolean = false,
        oasysId: String = UUID.randomUUID().toString(),
        id: Long = 0
    ) = OasysAssessment(
        oasysId,
        date,
        person,
        eventNumber,
        contact,
        court,
        offence,
        totalScore,
        description,
        assessedBy,
        riskFlags,
        concernFlags,
        dateCreated,
        dateReceived,
        initialSentencePlanDate,
        sentencePlanReviewDate,
        reviewTerminated,
        reviewNumber,
        layerType,
        ogrsScore1,
        ogrsScore2,
        ogpScore1,
        ogpScore2,
        ovpScore1,
        ovpScore2,
        softDeleted,
        id
    )
}