package uk.gov.justice.digital.hmpps.service

import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.ldap.findPreferenceByUsername

@Service
class LdapService(private val ldapTemplate: LdapTemplate) {
    @WithSpan
    fun findEmailForUsername(@SpanAttribute userName: String?) =
        userName?.let { ldapTemplate.findEmailByUsername(it) }
}