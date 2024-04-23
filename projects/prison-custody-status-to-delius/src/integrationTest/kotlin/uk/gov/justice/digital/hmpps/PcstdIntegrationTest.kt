package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.BookingGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.NotificationGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.notification.nomsId
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime

class PcstdIntegrationTest : PcstdIntegrationTestBase() {
    private val releaseOnLicence = "Released on Licence"

    @Test
    fun `release a prisoner`() {
        val notification = NotificationGenerator.PRISONER_RELEASED
        withBooking(BookingGenerator.RELEASED, BookingGenerator.RELEASED.lastMovement(notification.message.occurredAt))
        val nomsNumber = notification.nomsId()
        assertTrue(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertFalse(custody.isInCustody())

        verifyRelease(custody, notification.message.occurredAt, ReleaseTypeCode.ADULT_LICENCE)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, releaseOnLicence),
            CustodyEventTester(
                CustodyEventTypeCode.LOCATION_CHANGE,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]?.description
            )
        )

        verifyContact(custody, ContactType.Code.RELEASE_FROM_CUSTODY)

        verifyTelemetry("Released", "LocationUpdated", "StatusUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0001AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED",
                "movementReason" to "NCS",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `recall a prisoner`() {
        val notification = NotificationGenerator.PRISONER_RECEIVED
        withBooking(BookingGenerator.RECEIVED, BookingGenerator.RECEIVED.lastMovement(notification.message.occurredAt))
        val nomsNumber = notification.nomsId()
        assertFalse(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))
        assertThat(custody.statusChangeDate, equalTo(notification.message.occurredAt.toLocalDate()))
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))
        assertThat(custody.locationChangeDate!!, isCloseTo(notification.message.occurredAt))

        verifyRecall(custody, notification.message.occurredAt, RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, "Recall added in custody "),
            CustodyEventTester(CustodyEventTypeCode.LOCATION_CHANGE, InstitutionGenerator.DEFAULT.description)
        )

        val prisonManager = getPrisonManagers(nomsNumber).single()
        assertThat(prisonManager.date, isCloseTo(ZonedDateTime.now()))
        assertThat(prisonManager.allocationReason.code, equalTo("AUT"))

        verifyContacts(
            custody,
            4,
            ContactType.Code.BREACH_PRISON_RECALL,
            ContactType.Code.COMPONENT_TERMINATED,
            ContactType.Code.CHANGE_OF_INSTITUTION
        )

        licenceConditionRepository.findAll().filter {
            it.disposal.id == custody.disposal.id
        }.forEach {
            assertNotNull(it.terminationDate)
            assertThat(it.terminationReason?.code, equalTo("TEST"))
        }

        verifyTelemetry("Recalled", "LocationUpdated", "StatusUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to nomsNumber,
                "previousInstitution" to "WSI",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "R1",
                "movementType" to "Received"
            )
        }
    }

    @Test
    fun `when a prisoner is matched`() {
        val notification = NotificationGenerator.PRISONER_MATCHED
        val person = PersonGenerator.MATCHABLE
        withBooking(BookingGenerator.MATCHED, BookingGenerator.MATCHED.lastMovement(notification.message.occurredAt))
        val before = getCustody(person.nomsNumber)
        assertThat(before.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(person.nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.MOVED_TO.code))

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.LOCATION_CHANGE, InstitutionGenerator.MOVED_TO.description)
        )

        verifyContact(custody, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("RecallNotRequired", "PrisonerStatusCorrect", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to PersonGenerator.MATCHABLE.nomsNumber,
                "previousInstitution" to InstitutionGenerator.DEFAULT.nomisCdeCode!!,
                "institution" to InstitutionGenerator.MOVED_TO.nomisCdeCode!!,
                "reason" to "TRANSFERRED",
                "movementReason" to "INT",
                "movementType" to "Received"
            )
        }
    }

    @Test
    fun `a person died in custody alerts manager`() {
        val person = PersonGenerator.DIED
        val notification = NotificationGenerator.PRISONER_DIED
        withBooking(BookingGenerator.DIED, BookingGenerator.DIED.lastMovement(notification.message.occurredAt))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val dus = contactRepository.findAll().firstOrNull { it.person.id == person.id }
        assertNotNull(dus!!)
        assertThat(dus.type.code, equalTo(ContactType.Code.DIED_IN_CUSTODY.value))
        assertTrue(dus.alert!!)
        val personManager = personManagerRepository.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)
        assertThat(dus.teamId, equalTo(personManager.team.id))
        assertThat(dus.staffId, equalTo(personManager.staff.id))
        val alert = contactAlertRepository.findAll().firstOrNull { it.contactId == dus.id }
        assertNotNull(alert)

        verifyTelemetry("Died") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to person.nomsNumber,
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED",
                "movementReason" to "DEC",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `receieve a new custodial sentence`() {
        val notification = NotificationGenerator.PRISONER_NEW_CUSTODY
        withBooking(
            BookingGenerator.NEW_CUSTODY,
            BookingGenerator.NEW_CUSTODY.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        val before = getCustody(nomsNumber)
        assertThat(before.status.code, equalTo(CustodialStatusCode.SENTENCED_IN_CUSTODY.code))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))

        val recall = getRecalls(custody).singleOrNull()
        assertNull(recall)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, "In custody "),
            CustodyEventTester(CustodyEventTypeCode.LOCATION_CHANGE, InstitutionGenerator.DEFAULT.description)
        )

        val prisonManager = getPrisonManagers(nomsNumber).single()
        assertThat(prisonManager.date, isCloseTo(ZonedDateTime.now()))
        assertThat(prisonManager.allocationReason.code, equalTo("AUT"))

        verifyContact(custody, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("RecallNotRequired", "StatusUpdated", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0004AA",
                "previousInstitution" to "WSI",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "N",
                "movementType" to "Received"
            )
        }
    }

    @Test
    fun `receieve a prisoner already recalled in delius`() {
        val notification = NotificationGenerator.PRISONER_RECALLED
        withBooking(BookingGenerator.RECALLED, BookingGenerator.RECALLED.lastMovement(notification.message.occurredAt))
        val nomsNumber = notification.nomsId()

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))
        assertThat(custody.statusChangeDate, equalTo(notification.message.occurredAt.toLocalDate()))
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))
        assertThat(custody.locationChangeDate!!, isCloseTo(notification.message.occurredAt))

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, "In custody "),
            CustodyEventTester(CustodyEventTypeCode.LOCATION_CHANGE, InstitutionGenerator.DEFAULT.description)
        )

        val prisonManager = getPrisonManagers(nomsNumber).single()
        assertThat(prisonManager.date, isCloseTo(ZonedDateTime.now()))
        assertThat(prisonManager.allocationReason.code, equalTo("AUT"))

        verifyContact(custody, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("RecallNotRequired", "StatusUpdated", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0006AA",
                "previousInstitution" to "WSI",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "movementReason" to "24",
                "movementType" to "Received"
            )
        }
    }

    @Test
    fun `hospital release when released on licence in delius`() {
        whenever(featureFlags.enabled("messages_released_hospital")).thenReturn(true)
        val notification = NotificationGenerator.PRISONER_HOSPITAL_RELEASED
        withBooking(
            BookingGenerator.HOSPITAL_RELEASE,
            BookingGenerator.HOSPITAL_RELEASE.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        assertFalse(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionCode.OTHER_SECURE_UNIT.code))
        assertThat(custody.locationChangeDate!!, isCloseTo(notification.message.occurredAt))
        assertThat(custody.status.code, equalTo(CustodialStatusCode.RECALLED.code))
        assertThat(custody.statusChangeDate, equalTo(notification.message.occurredAt.toLocalDate()))

        verifyRecall(custody, notification.message.occurredAt, RecallReason.Code.TRANSFER_TO_SECURE_HOSPITAL)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, "Transfer to/from Hospital"),
            CustodyEventTester(
                CustodyEventTypeCode.LOCATION_CHANGE,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.OTHER_SECURE_UNIT]?.description
            )
        )

        verifyContacts(custody, 2, ContactType.Code.BREACH_PRISON_RECALL, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("Recalled", "StatusUpdated", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0005AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED_TO_HOSPITAL",
                "movementReason" to "HO",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `hospital release when in custody in delius`() {
        whenever(featureFlags.enabled("messages_released_hospital")).thenReturn(true)
        val notification = NotificationGenerator.PRISONER_HOSPITAL_IN_CUSTODY
        withBooking(
            BookingGenerator.HOSPITAL_CUSTODY,
            BookingGenerator.HOSPITAL_CUSTODY.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        assertTrue(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(
            custody.institution?.code,
            equalTo(InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.OTHER_SECURE_UNIT]?.code)
        )
        assertThat(custody.locationChangeDate!!, isCloseTo(notification.message.occurredAt))
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))

        verifyCustodyHistory(
            custody,
            CustodyEventTester(
                CustodyEventTypeCode.LOCATION_CHANGE,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.OTHER_SECURE_UNIT]?.description
            )
        )

        verifyContact(custody, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("RecallNotRequired", "PrisonerStatusCorrect", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0007AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED_TO_HOSPITAL",
                "movementReason" to "HQ",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `received into prison when on rotl`() {
        val notification = NotificationGenerator.PRISONER_ROTL_RETURN
        withBooking(
            BookingGenerator.ROTL_RETURN,
            BookingGenerator.ROTL_RETURN.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        assertFalse(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))

        verifyRecall(custody, notification.message.occurredAt, RecallReason.Code.END_OF_TEMPORARY_LICENCE)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, "Recall added in custody "),
            CustodyEventTester(CustodyEventTypeCode.LOCATION_CHANGE, InstitutionGenerator.DEFAULT.description)
        )

        verifyContacts(custody, 2, ContactType.Code.BREACH_PRISON_RECALL, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("Recalled", "StatusUpdated", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0008AA",
                "previousInstitution" to "WSI",
                "institution" to "WSI",
                "reason" to "TEMPORARY_ABSENCE_RETURN",
                "movementReason" to "24",
                "movementType" to "Received"
            )
        }
    }

    @Test
    fun `IRC release when released on licence in delius`() {
        val notification = NotificationGenerator.PRISONER_IRC_RELEASED
        withBooking(
            BookingGenerator.IRC_RELEASED,
            BookingGenerator.IRC_RELEASED.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        assertFalse(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionCode.OTHER_IRC.code))
        assertThat(custody.status.code, equalTo(CustodialStatusCode.RECALLED.code))

        verifyRecall(custody, notification.message.occurredAt, RecallReason.Code.TRANSFER_TO_IRC)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, "Transfer to Immigration Removal Centre"),
            CustodyEventTester(
                CustodyEventTypeCode.LOCATION_CHANGE,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.OTHER_IRC]?.description
            )
        )

        verifyContacts(custody, 2, ContactType.Code.BREACH_PRISON_RECALL, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("Recalled", "StatusUpdated", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0009AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED",
                "movementReason" to "DE",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `irc release when in custody in delius`() {
        val notification = NotificationGenerator.PRISONER_IRC_IN_CUSTODY
        withBooking(
            BookingGenerator.IRC_CUSTODY,
            BookingGenerator.IRC_CUSTODY.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        assertTrue(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionCode.OTHER_IRC.code))
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))

        verifyCustodyHistory(
            custody,
            CustodyEventTester(
                CustodyEventTypeCode.LOCATION_CHANGE,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.OTHER_IRC]?.description
            )
        )

        verifyContact(custody, ContactType.Code.CHANGE_OF_INSTITUTION)

        verifyTelemetry("RecallNotRequired", "PrisonerStatusCorrect", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0011AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED",
                "movementReason" to "DD",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `release a ecsl prisoner with feature active`() {
        val notification = NotificationGenerator.PRISONER_RELEASED_ECSL_ACTIVE
        withBooking(
            BookingGenerator.ECSL_ACTIVE,
            BookingGenerator.ECSL_ACTIVE.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        assertTrue(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertFalse(custody.isInCustody())

        verifyRelease(custody, notification.message.occurredAt, ReleaseTypeCode.END_CUSTODY_SUPERVISED_LICENCE)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, releaseOnLicence),
            CustodyEventTester(
                CustodyEventTypeCode.LOCATION_CHANGE,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]?.description
            )
        )

        verifyContact(custody, ContactType.Code.RELEASE_FROM_CUSTODY)

        verifyTelemetry("Released", "LocationUpdated", "StatusUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0013AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED",
                "movementReason" to "ECSL",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `prisoner absconded - unlawfully at large`() {
        val notification = NotificationGenerator.PRISONER_ABSCONDED
        withBooking(
            BookingGenerator.ABSCONDED,
            BookingGenerator.ABSCONDED.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()
        assertFalse(getCustody(nomsNumber).isInCustody())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val ual = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNLAWFULLY_AT_LARGE]!!
        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(ual.code))
        assertThat(custody.status.code, equalTo(CustodialStatusCode.RECALLED.code))
        assertThat(custody.statusChangeDate, equalTo(notification.message.occurredAt.toLocalDate()))

        verifyRecall(custody, notification.message.occurredAt, RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT)

        verifyCustodyHistory(
            custody,
            CustodyEventTester(CustodyEventTypeCode.STATUS_CHANGE, "Recall added unlawfully at large "),
            CustodyEventTester(CustodyEventTypeCode.LOCATION_CHANGE, ual.description)
        )

        verifyContacts(
            custody,
            2,
            ContactType.Code.BREACH_PRISON_RECALL,
            ContactType.Code.CHANGE_OF_INSTITUTION
        )

        licenceConditionRepository.findAll().filter {
            it.disposal.id == custody.disposal.id
        }.forEach {
            assertNotNull(it.terminationDate)
            assertThat(it.terminationReason?.code, equalTo("TEST"))
        }

        verifyTelemetry("Recalled", "LocationUpdated", "StatusUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to nomsNumber,
                "reason" to "RELEASED",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "movementReason" to "UAL",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `etr release when in custody in delius`() {
        val notification = NotificationGenerator.PRISONER_ETR_IN_CUSTODY
        withBooking(
            BookingGenerator.ETR_CUSTODY,
            BookingGenerator.ETR_CUSTODY.lastMovement(notification.message.occurredAt)
        )
        val nomsNumber = notification.nomsId()

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.status.code, equalTo(CustodialStatusCode.IN_CUSTODY.code))
        assertThat(custody.institution?.code, equalTo(InstitutionCode.OTHER_IRC.code))

        verifyContact(custody, ContactType.Code.CHANGE_OF_INSTITUTION)
        verifyCustodyHistory(
            custody,
            CustodyEventTester(
                CustodyEventTypeCode.LOCATION_CHANGE,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.OTHER_IRC]?.description
            )
        )

        verifyTelemetry("RecallNotRequired", "PrisonerStatusCorrect", "LocationUpdated") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0019AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED",
                "movementReason" to "ETR",
                "movementType" to "Released"
            )
        }
    }

    @Test
    fun `administrative release is not released`() {
        val notification = NotificationGenerator.PRISONER_ADMIN_MERGE
        withBooking(
            BookingGenerator.ADMIN_MERGE,
            BookingGenerator.ADMIN_MERGE.lastMovement(notification.message.occurredAt)
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verifyTelemetry("NoActions") {
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "A0021AA",
                "previousInstitution" to "WSI",
                "institution" to "OUT",
                "reason" to "RELEASED",
                "movementReason" to "MRG",
                "movementType" to "Released"
            )
        }
    }
}
