package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.repository.StaffRepository
import uk.gov.justice.digital.hmpps.integration.EntityType
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.service.StatusChangeService
import uk.gov.justice.digital.hmpps.entity.staff.UserRepository
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StatusChangeServiceStaffResolutionIntegrationTest @Autowired constructor(
    private val statusChangeService: StatusChangeService,
    private val contactRepository: ContactRepository,
    private val staffRepository: StaffRepository,
    private val userRepository: UserRepository,
) {

    @Test
    fun `staff is resolved from user when user has staff`() {
        // Given: A user with staff (TestData.USER already has TestData.STAFF)
        // When: Status change is processed with that user
        val messageId = UUID.randomUUID()
        val occurredAt = ZonedDateTime.now()

        statusChangeService.statusChanged(
            messageId,
            crn = TestData.PERSON.crn,
            occurredAt = occurredAt,
            info = StatusInfo(
                newStatus = StatusInfo.Status.BREACH,
                sourcedFromEntityType = EntityType.LICENCE_CONDITION,
                sourcedFromEntityId = TestData.LICENCE_CONDITIONS.first().id,
                notes = "Breach notes",
                description = "Breach description",
                username = TestData.USER.username
            ),
        )

        // Then: Contact is created with user's staff, not manager's staff
        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id &&
                it.type.code == StatusInfo.Status.BREACH.contactTypeCode &&
                it.externalReference?.contains(messageId.toString()) == true
        }
        assertThat(contact).isNotNull
        assertThat(contact?.staff?.id).isEqualTo(TestData.USER.staff?.id)
    }

    @Test
    fun `staff falls back to manager when user not found`() {
        // Given: No user exists with the username
        val messageId = UUID.randomUUID()
        val occurredAt = ZonedDateTime.now()

        // When: Status change is processed with non-existent username
        statusChangeService.statusChanged(
            messageId,
            crn = TestData.PERSON.crn,
            occurredAt = occurredAt,
            info = StatusInfo(
                newStatus = StatusInfo.Status.ON_PROGRAMME,
                sourcedFromEntityType = EntityType.REQUIREMENT,
                sourcedFromEntityId = TestData.REQUIREMENTS.first().id,
                notes = "On programme notes",
                description = "On programme description",
                username = "nonexistentuser"
            ),
        )

        // Then: Contact is created with manager's staff as fallback
        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id &&
                it.type.code == StatusInfo.Status.ON_PROGRAMME.contactTypeCode &&
                it.externalReference?.contains(messageId.toString()) == true
        }
        assertThat(contact).isNotNull
        assertThat(contact?.staff?.id).isEqualTo(TestData.MANAGER.staff.id)
    }

    @Test
    fun `staff falls back to manager when user has no staff`() {
        // Given: We'll use an existing user without staff (TestData.USER_WITH_LIMITED_ACCESS has no staff)
        // When: Status change is processed
        val messageId = UUID.randomUUID()
        val occurredAt = ZonedDateTime.now()

        statusChangeService.statusChanged(
            messageId,
            crn = TestData.PERSON.crn,
            occurredAt = occurredAt,
            info = StatusInfo(
                newStatus = StatusInfo.Status.PROGRAMME_COMPLETE,
                sourcedFromEntityType = EntityType.LICENCE_CONDITION,
                sourcedFromEntityId = TestData.LICENCE_CONDITIONS[1].id,
                notes = "Programme complete notes",
                description = "Programme complete description",
                username = TestData.USER_WITH_LIMITED_ACCESS.username
            ),
        )

        // Then: Contact is created with manager's staff as fallback
        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id &&
                it.type.code == StatusInfo.Status.PROGRAMME_COMPLETE.contactTypeCode &&
                it.externalReference?.contains(messageId.toString()) == true
        }
        assertThat(contact).isNotNull
        assertThat(contact?.staff?.id).isEqualTo(TestData.MANAGER.staff.id)
    }

    @Test
    fun `contact has correct external reference and other fields`() {
        // Given: A valid user with staff
        // When: Status change is processed
        val messageId = UUID.randomUUID()
        val occurredAt = ZonedDateTime.now()

        statusChangeService.statusChanged(
            messageId,
            crn = TestData.PERSON.crn,
            occurredAt = occurredAt,
            info = StatusInfo(
                newStatus = StatusInfo.Status.BREACH,
                sourcedFromEntityType = EntityType.LICENCE_CONDITION,
                sourcedFromEntityId = TestData.LICENCE_CONDITIONS.first().id,
                notes = "Test breach notes",
                description = "Test breach description",
                username = TestData.USER.username
            ),
        )

        // Then: Contact has correct properties
        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id &&
                it.type.code == StatusInfo.Status.BREACH.contactTypeCode &&
                it.externalReference?.contains(messageId.toString()) == true
        }
        assertThat(contact).isNotNull
        assertThat(contact?.externalReference).isEqualTo("urn:uk:gov:hmpps:accredited-programmes-service:$messageId")
        assertThat(contact?.notes).isEqualTo("Test breach notes")
        assertThat(contact?.description).isEqualTo("Test breach description")
        assertThat(contact?.person?.crn).isEqualTo(TestData.PERSON.crn)
        assertThat(contact?.team?.id).isEqualTo(TestData.MANAGER.team.id)
        assertThat(contact?.provider?.id).isEqualTo(TestData.MANAGER.team.provider.id)
        assertThat(contact?.staff?.id).isEqualTo(TestData.USER.staff?.id)
    }
}


