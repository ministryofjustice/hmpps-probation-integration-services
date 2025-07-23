package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResource
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean

@Profile("secure")
@Configuration
class WebServiceSecurityConfig(
    @Value("\${keystore-password}") private val keystorePassword: String,
    @Value("\${private-key-password}") private val privateKeyPassword: String,
    @Value("\${ws-sec.request-encrypt-actions}") private val requestActions: String,
    @Value("\${ws-sec.response-encrypt-actions}") private val responseActions: String,
    @Value("\${ws-sec.response-signature-parts}") private val responseSignatureParts: String,
    @Value("\${ws-sec.response-encryption-parts}") private val responseEncryptionParts: String,
    @Value("\${trusted-cert-alias-name}") private val trustedCertAliasName: String,
    @Value("\${private-key-alias-name}") private val privateKeyAliasName: String,
    @Value("\${ws-sec.keystore-file-path}") private val keystoreFilePath: String,
    @Value("\${ws-sec.encryption-sym-algorithm}") private val encryptionAlgorithm: String,
) {
    @Bean
    @Throws(Exception::class)
    fun keyStoreCallbackHandler(): KeyStoreCallbackHandler = KeyStoreCallbackHandler().apply {
        setPrivateKeyPassword(privateKeyPassword)
    }

    @Bean
    fun getValidationCryptoFactoryBean(): CryptoFactoryBean = CryptoFactoryBean().apply {
        setKeyStoreLocation(FileSystemResource(keystoreFilePath))
        setKeyStorePassword(keystorePassword)
        setDefaultX509Alias(trustedCertAliasName)
    }

    @Bean
    @Throws(Exception::class)
    fun securityInterceptor(): Wss4jSecurityInterceptor = Wss4jSecurityInterceptor().apply {

        // validate incoming request
        setValidationActions(requestActions)
        setValidationSignatureCrypto(getValidationCryptoFactoryBean().getObject())
        setValidationDecryptionCrypto(getValidationCryptoFactoryBean().getObject())
        setValidationCallbackHandler(keyStoreCallbackHandler())

        // encrypt the response
        setSecurementEncryptionUser(privateKeyAliasName)
        setSecurementEncryptionParts(responseEncryptionParts)
        setSecurementEncryptionCrypto(getValidationCryptoFactoryBean().getObject())
        setSecurementEncryptionSymAlgorithm(encryptionAlgorithm)

        // sign the response
        setSecurementActions(responseActions)
        setSecurementUsername(trustedCertAliasName)
        setSecurementPassword(privateKeyPassword)
        setSecurementSignatureParts(responseSignatureParts)
        setSecurementSignatureCrypto(getValidationCryptoFactoryBean().getObject())
    }
}
