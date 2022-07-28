package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.exceptions.BusinessInteractionNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.AuditedInteractionService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class AuditedInteractionTest {

    @Mock
    lateinit var businessInteractionRepository: BusinessInteractionRepository

    @Mock
    lateinit var auditedInteractionRepository: AuditedInteractionRepository

    private val principal = ServicePrincipal(UserGenerator.APPLICATION_USER.username, UserGenerator.APPLICATION_USER.id)

    @Mock
    private lateinit var authentication: Authentication

    @Mock
    private lateinit var securityContext: SecurityContext

    @InjectMocks
    lateinit var auditedInteractionService: AuditedInteractionService

    @Test
    fun `create audited interaction`() {
        val userId = 123L
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(authentication.principal).thenReturn(ServicePrincipal("username", userId))
        SecurityContextHolder.setContext(securityContext)

        val bi = BusinessInteraction(1, BusinessInteractionCode.CASE_NOTES_MERGE.code, ZonedDateTime.now())
        whenever(businessInteractionRepository.findByCode(eq(BusinessInteractionCode.CASE_NOTES_MERGE.code), any()))
            .thenReturn(bi)

        val parameters = AuditedInteraction.Parameters(
            Pair("key", "value")
        )
        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.CASE_NOTES_MERGE,
            parameters
        )
        val aiCaptor = ArgumentCaptor.forClass(AuditedInteraction::class.java)
        verify(auditedInteractionRepository, Mockito.times(1)).save(aiCaptor.capture())
        val saved = aiCaptor.value

        assertThat(saved.businessInteractionId, Matchers.equalTo(1))
        assertThat(saved.outcome, Matchers.equalTo(AuditedInteraction.Outcome.SUCCESS))
        assertThat(saved.userId, Matchers.equalTo(userId))
        assertThat(saved.parameters, Matchers.equalTo(parameters))
    }

    @Test
    fun `create audited interaction no bi code`() {
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(authentication.principal).thenReturn(principal)
        SecurityContextHolder.setContext(securityContext)
        whenever(businessInteractionRepository.findByCode(any(), any())).thenReturn(null)

        val parameters = AuditedInteraction.Parameters(
            Pair("key", "value")
        )
        assertThrows<BusinessInteractionNotFoundException> {
            auditedInteractionService.createAuditedInteraction(
                BusinessInteractionCode.CASE_NOTES_MERGE,
                parameters
            )
        }
    }

    @Test
    fun `create audited interaction no service principal`() {
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(authentication.principal).thenReturn(null)
        SecurityContextHolder.setContext(securityContext)

        val parameters = AuditedInteraction.Parameters(
            Pair("key", "value")
        )
        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.CASE_NOTES_MERGE,
            parameters
        )
        verify(auditedInteractionRepository, times(0)).save(any())
    }

    @Test
    fun `create audited interaction no authentication`() {
        whenever(securityContext.authentication).thenReturn(null)
        SecurityContextHolder.setContext(securityContext)

        val parameters = AuditedInteraction.Parameters(
            Pair("key", "value")
        )
        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.CASE_NOTES_MERGE,
            parameters
        )
        verify(auditedInteractionRepository, times(0)).save(any())
    }

    @Test
    fun `test audit interaction class`() {
        val dateTime = ZonedDateTime.now()
        val auditedInteraction = AuditedInteraction(
            111, 222, dateTime, AuditedInteraction.Outcome.SUCCESS,
            AuditedInteraction.Parameters(
                Pair("key", "value")
            )
        )
        assertThat(auditedInteraction.businessInteractionId, Matchers.equalTo(111))
        assertThat(auditedInteraction.userId, Matchers.equalTo(222))
        assertThat(auditedInteraction.dateTime, Matchers.equalTo(dateTime))
        assertThat(auditedInteraction.outcome, Matchers.equalTo(AuditedInteraction.Outcome.SUCCESS))
        assertThat(
            auditedInteraction.parameters, Matchers.equalTo(AuditedInteraction.Parameters(Pair("key", "value")))
        )
    }

    @Test
    fun `test audit interaction class defaults`() {
        val auditedInteraction = AuditedInteraction(
            111, 222
        )
        assertThat(auditedInteraction.businessInteractionId, Matchers.equalTo(111))
        assertThat(auditedInteraction.userId, Matchers.equalTo(222))
        assertThat(auditedInteraction.dateTime, Matchers.equalTo(auditedInteraction.dateTime))
        assertThat(auditedInteraction.outcome, Matchers.equalTo(AuditedInteraction.Outcome.SUCCESS))
        assertThat(
            auditedInteraction.parameters, Matchers.equalTo(AuditedInteraction.Parameters())
        )
    }

    @Test
    fun `test audit interaction id class defaults`() {
        val auditedInteractionId = AuditedInteractionId()
        assertThat(auditedInteractionId.businessInteractionId, Matchers.equalTo(0))
        assertThat(auditedInteractionId.userId, Matchers.equalTo(0))
        assertThat(auditedInteractionId.dateTime, Matchers.equalTo(auditedInteractionId.dateTime))
    }

    @Test
    fun `test audit interaction id class`() {
        val dateTime = ZonedDateTime.now()
        val auditedInteractionId = AuditedInteractionId(dateTime, 111, 222)
        assertThat(auditedInteractionId.businessInteractionId, Matchers.equalTo(111))
        assertThat(auditedInteractionId.userId, Matchers.equalTo(222))
        assertThat(auditedInteractionId.dateTime, Matchers.equalTo(dateTime))
    }

    @Test
    fun `business interaction class defaults`() {
        val dateTime = ZonedDateTime.now()
        val businessInteraction = BusinessInteraction(111, "code", dateTime)
        assertThat(businessInteraction.id, Matchers.equalTo(111))
        assertThat(businessInteraction.code, Matchers.equalTo("code"))
        assertThat(businessInteraction.enabledDate, Matchers.equalTo(dateTime))
    }
}
