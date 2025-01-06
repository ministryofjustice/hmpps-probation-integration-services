package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Referral
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Residence
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApprovedPremisesCategoryCode
import uk.gov.justice.digital.hmpps.service.EXT_REF_BOOKING_PREFIX
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

object ReferralGenerator {
    val EXISTING_REFERRAL = generateReferral()

    val BOOKING_ID = UUID.randomUUID().toString()
    var BOOKING_WITHOUT_ARRIVAL = generateReferral(
        person = PersonGenerator.PERSON_WITH_BOOKING,
        referralNotes = "Some other notes",
        externalReference = "${EXT_REF_BOOKING_PREFIX}$BOOKING_ID",
    )

    val ARRIVED_ID = UUID.randomUUID().toString()
    var BOOKING_ARRIVED = generateReferral(
        person = PersonGenerator.PERSON_WITH_BOOKING,
        referralNotes = "Some other notes",
        externalReference = "${EXT_REF_BOOKING_PREFIX}$ARRIVED_ID",
        expectedArrivalDate = LocalDate.now(),
        expectedDepartureDate = LocalDate.now().plusDays(7),
    )

    var ARRIVAL = generateResidence(PersonGenerator.PERSON_WITH_BOOKING, BOOKING_ARRIVED)

    val DEPARTED_ID = UUID.randomUUID().toString()
    var BOOKING_DEPARTED = generateReferral(
        person = PersonGenerator.PERSON_WITH_BOOKING,
        referralNotes = "Some other notes",
        externalReference = "${EXT_REF_BOOKING_PREFIX}$DEPARTED_ID",
        expectedArrivalDate = LocalDate.now().minusDays(8),
        expectedDepartureDate = LocalDate.now().minusDays(1)
    )

    var DEPARTURE = generateResidence(
        PersonGenerator.PERSON_WITH_BOOKING, BOOKING_DEPARTED,
        departureDateTime = ZonedDateTime.of(
            BOOKING_DEPARTED.expectedDepartureDate,
            LocalTime.now(EuropeLondon).minusHours(1),
            EuropeLondon
        )
    )

    fun generateReferral(
        person: Person = PersonGenerator.DEFAULT,
        eventId: Long = PersonGenerator.ANOTHER_EVENT.id,
        approvedPremises: ApprovedPremises = ApprovedPremisesGenerator.DEFAULT,
        referralDate: LocalDate = LocalDate.now(),
        expectedArrivalDate: LocalDate? = LocalDate.now().plusDays(1),
        expectedDepartureDate: LocalDate? = LocalDate.now().plusDays(8),
        decisionDate: ZonedDateTime? = ZonedDateTime.now(EuropeLondon),
        referralNotes: String? = null,
        externalReference: String? = null
    ) = Referral(
        personId = person.id,
        eventId = eventId,
        approvedPremisesId = approvedPremises.id,
        referralDate = referralDate,
        referralDateTypeId = ReferenceDataGenerator.REFERRAL_DATE_TYPE.id,
        expectedArrivalDate = expectedArrivalDate,
        expectedDepartureDate = expectedDepartureDate,
        decisionDate = decisionDate?.truncatedTo(ChronoUnit.SECONDS)?.withZoneSameInstant(EuropeLondon),
        categoryId = ReferenceDataGenerator.REFERRAL_CATEGORIES[ApprovedPremisesCategoryCode.OTHER.value]!!.id,
        referralGroupId = ReferenceDataGenerator.REFERRAL_GROUP.id,
        decisionId = ReferenceDataGenerator.ACCEPTED_DEFERRED_ADMISSION.id,
        referralNotes = referralNotes,
        decisionNotes = null,
        referralSourceId = ReferenceDataGenerator.OTHER_REFERRAL_SOURCE.id,
        sourceTypeId = ReferenceDataGenerator.AP_REFERRAL_SOURCE.id,
        reasonForReferral = null,
        activeArsonRiskId = ReferenceDataGenerator.YN_UNKNOWN.id,
        arsonRiskDetails = null,
        disabilityIssuesId = ReferenceDataGenerator.YN_UNKNOWN.id,
        disabilityDetails = null,
        singleRoomId = ReferenceDataGenerator.YN_UNKNOWN.id,
        singleRoomDetails = null,
        sexOffender = false,
        gangAffiliated = false,
        rohChildrenId = ReferenceDataGenerator.RISK_UNKNOWN.id,
        rohKnownPersonId = ReferenceDataGenerator.RISK_UNKNOWN.id,
        rohOthersId = ReferenceDataGenerator.RISK_UNKNOWN.id,
        rohPublicId = ReferenceDataGenerator.RISK_UNKNOWN.id,
        rohResidentsId = ReferenceDataGenerator.RISK_UNKNOWN.id,
        rohSelfId = ReferenceDataGenerator.RISK_UNKNOWN.id,
        rohStaffId = ReferenceDataGenerator.RISK_UNKNOWN.id,
        riskInformation = null,
        decisionTeamId = TeamGenerator.APPROVED_PREMISES_TEAM.id,
        decisionStaffId = 26553,
        referringTeamId = TeamGenerator.UNALLOCATED.id,
        referringStaffId = 563828,
        externalReference = externalReference
    )

    fun generateResidence(
        person: Person,
        referral: Referral,
        approvedPremisesId: Long = ApprovedPremisesGenerator.DEFAULT.id,
        arrivalNotes: String? = null,
        keyWorkerStaffId: Long? = StaffGenerator.DEFAULT_STAFF.id,
        arrivalDateTime: ZonedDateTime = ZonedDateTime.of(
            referral.expectedArrivalDate,
            LocalTime.now(EuropeLondon),
            EuropeLondon
        ),
        departureDateTime: ZonedDateTime? = null
    ) = Residence(
        person.id,
        referral.id,
        approvedPremisesId,
        arrivalDateTime.truncatedTo(ChronoUnit.SECONDS),
        arrivalNotes,
        keyWorkerStaffId
    ).apply {
        this.departureDate = departureDateTime?.truncatedTo(ChronoUnit.SECONDS)
    }
}
