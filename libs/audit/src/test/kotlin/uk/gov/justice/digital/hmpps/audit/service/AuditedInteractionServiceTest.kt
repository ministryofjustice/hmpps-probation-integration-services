package uk.gov.justice.digital.hmpps.audit.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.context.event.ApplicationStartedEvent
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionNotFoundException
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteractionId
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.AuditUser
import uk.gov.justice.digital.hmpps.user.AuditUserService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class AuditedInteractionServiceTest {

    @Mock
    lateinit var businessInteractionRepository: BusinessInteractionRepository

    @Mock
    lateinit var auditedInteractionRepository: AuditedInteractionRepository

    @Mock
    lateinit var auditUserService: AuditUserService

    @Mock
    lateinit var applicationStartedEvent: ApplicationStartedEvent

    @InjectMocks
    lateinit var auditedInteractionService: AuditedInteractionService

    private val user = AuditUser(1, "ServiceUserName")

    private fun setupUser() {
        whenever(auditUserService.findUser(user.username)).thenReturn(user)
        ServiceContext(user.username, auditUserService).onApplicationEvent(applicationStartedEvent)
    }

    @Test
    fun `create audited interaction`() {
        setupUser()

        val bi = BusinessInteraction(1, BusinessInteractionCode.TEST_BI_CODE.code, ZonedDateTime.now())
        whenever(businessInteractionRepository.findByCode(eq(BusinessInteractionCode.TEST_BI_CODE.code), any()))
            .thenReturn(bi)

        val parameters = AuditedInteraction.Parameters(
            Pair("key", "value")
        )
        val dateTime = ZonedDateTime.now()
        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.TEST_BI_CODE,
            parameters,
            AuditedInteraction.Outcome.SUCCESS,
            dateTime,
            null
        )
        val aiCaptor = ArgumentCaptor.forClass(AuditedInteraction::class.java)
        verify(auditedInteractionRepository, Mockito.times(1)).save(aiCaptor.capture())
        val saved = aiCaptor.value

        assertThat(saved.businessInteractionId, equalTo(1))
        assertThat(saved.outcome, equalTo(AuditedInteraction.Outcome.SUCCESS))
        assertThat(saved.userId, equalTo(user.id))
        assertThat(saved.parameters, equalTo(parameters))
        assertThat(saved.dateTime, equalTo(dateTime))
    }

    @Test
    fun `create audited interaction no bi code`() {
        setupUser()
        whenever(businessInteractionRepository.findByCode(any(), any())).thenReturn(null)

        val parameters = AuditedInteraction.Parameters(
            Pair("key", "value")
        )
        assertThrows<BusinessInteractionNotFoundException> {
            auditedInteractionService.createAuditedInteraction(
                BusinessInteractionCode.TEST_BI_CODE,
                parameters,
                AuditedInteraction.Outcome.SUCCESS,
                ZonedDateTime.now(),
                null
            )
        }
    }

    @Test
    fun `create audited interaction with username override`() {
        setupUser()
        val anotherUser = AuditUser(99, "AnotherUser")
        whenever(auditUserService.findUser(anotherUser.username)).thenReturn(anotherUser)

        val bi = BusinessInteraction(1, BusinessInteractionCode.TEST_BI_CODE.code, ZonedDateTime.now())
        whenever(businessInteractionRepository.findByCode(eq(BusinessInteractionCode.TEST_BI_CODE.code), any()))
            .thenReturn(bi)

        val parameters = AuditedInteraction.Parameters(
            Pair("key", "value")
        )
        val dateTime = ZonedDateTime.now()
        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.TEST_BI_CODE,
            parameters,
            AuditedInteraction.Outcome.SUCCESS,
            dateTime,
            anotherUser.username
        )
        val aiCaptor = ArgumentCaptor.forClass(AuditedInteraction::class.java)
        verify(auditedInteractionRepository, Mockito.times(1)).save(aiCaptor.capture())
        val saved = aiCaptor.value

        assertThat(saved.businessInteractionId, equalTo(1))
        assertThat(saved.outcome, equalTo(AuditedInteraction.Outcome.SUCCESS))
        assertThat(saved.userId, equalTo(anotherUser.id))
        assertThat(saved.parameters, equalTo(parameters))
        assertThat(saved.dateTime, equalTo(dateTime))
    }

    @Test
    fun `test audit interaction class`() {
        val dateTime = ZonedDateTime.now()
        val auditedInteraction = AuditedInteraction(
            111,
            222,
            dateTime,
            AuditedInteraction.Outcome.SUCCESS,
            AuditedInteraction.Parameters(
                Pair("key", "value")
            )
        )
        assertThat(auditedInteraction.businessInteractionId, equalTo(111))
        assertThat(auditedInteraction.userId, equalTo(222))
        assertThat(auditedInteraction.dateTime, equalTo(dateTime))
        assertThat(auditedInteraction.outcome, equalTo(AuditedInteraction.Outcome.SUCCESS))
        assertThat(
            auditedInteraction.parameters,
            equalTo(AuditedInteraction.Parameters(Pair("key", "value")))
        )
    }

    @Test
    fun `test audit interaction class defaults`() {
        val auditedInteraction = AuditedInteraction(
            111,
            222
        )
        assertThat(auditedInteraction.businessInteractionId, equalTo(111))
        assertThat(auditedInteraction.userId, equalTo(222))
        assertThat(auditedInteraction.dateTime, equalTo(auditedInteraction.dateTime))
        assertThat(auditedInteraction.outcome, equalTo(AuditedInteraction.Outcome.SUCCESS))
        assertThat(
            auditedInteraction.parameters,
            equalTo(AuditedInteraction.Parameters())
        )
    }

    @Test
    fun `test audit interaction id class defaults`() {
        val auditedInteractionId = AuditedInteractionId()
        assertThat(auditedInteractionId.businessInteractionId, equalTo(0))
        assertThat(auditedInteractionId.userId, equalTo(0))
        assertThat(auditedInteractionId.dateTime, equalTo(auditedInteractionId.dateTime))
    }

    @Test
    fun `test audit interaction id class`() {
        val dateTime = ZonedDateTime.now()
        val auditedInteractionId = AuditedInteractionId(dateTime, 111, 222)
        assertThat(auditedInteractionId.businessInteractionId, equalTo(111))
        assertThat(auditedInteractionId.userId, equalTo(222))
        assertThat(auditedInteractionId.dateTime, equalTo(dateTime))
    }

    @Test
    fun `business interaction class defaults`() {
        val dateTime = ZonedDateTime.now()
        val businessInteraction = BusinessInteraction(111, "code", dateTime)
        assertThat(businessInteraction.id, equalTo(111))
        assertThat(businessInteraction.code, equalTo("code"))
        assertThat(businessInteraction.enabledDate, equalTo(dateTime))
    }
}
