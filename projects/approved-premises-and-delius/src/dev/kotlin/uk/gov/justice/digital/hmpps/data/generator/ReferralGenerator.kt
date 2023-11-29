package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Referral
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApprovedPremisesCategoryCode
import java.time.LocalDate
import java.time.ZonedDateTime

object ReferralGenerator {
    val EXISTING_REFERRAL = generateReferral()

    fun generateReferral(
        person: Person = PersonGenerator.DEFAULT,
        eventId: Long = PersonGenerator.ANOTHER_EVENT.id,
        approvedPremises: ApprovedPremises = ApprovedPremisesGenerator.DEFAULT,
        referralDate: LocalDate = LocalDate.now(),
        expectedArrivalDate: LocalDate? = LocalDate.now().plusDays(1),
        expectedDepartureDate: LocalDate? = LocalDate.now().plusDays(8),
        decisionDate: ZonedDateTime? = ZonedDateTime.now()
    ) = Referral(
        personId = person.id,
        eventId = eventId,
        approvedPremisesId = approvedPremises.id,
        referralDate = referralDate,
        referralDateTypeId = ReferenceDataGenerator.REFERRAL_DATE_TYPE.id,
        expectedArrivalDate = expectedArrivalDate,
        expectedDepartureDate = expectedDepartureDate,
        decisionDate = decisionDate,
        categoryId = ReferenceDataGenerator.REFERRAL_CATEGORIES[ApprovedPremisesCategoryCode.OTHER.value]!!.id,
        decisionId = ReferenceDataGenerator.ACCEPTED_DEFERRED_ADMISSION.id,
        referralNotes = null,
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
        referringStaffId = 563828
    )
}
