package uk.gov.justice.digital.hmpps.integrations.newtech

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Service
class NewTechEncoder(
    @Value("\${integrations.new-tech.secret}") private val newTechServiceSecret: String
) {
    /**
     * Encrypts and base64-encodes the input, as expected by the "new-tech" service.
     * See https://github.com/ministryofjustice/ndelius-new-tech/blob/main/app/helpers/Encryption.java
     */
    fun encode(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
            .digest(newTechServiceSecret.toByteArray()).copyOf(16)
        val secret = SecretKeySpec(digest, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secret)

        val encrypted = cipher.doFinal(input.toByteArray())
        val base64Encoded = Base64.getEncoder().encodeToString(encrypted)
        return URLEncoder.encode(base64Encoded, Charsets.UTF_8)
    }
}
