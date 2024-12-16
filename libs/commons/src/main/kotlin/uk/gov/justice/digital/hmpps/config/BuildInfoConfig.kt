package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.info.GitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

/**
 * Read build and git info from environment variables into the /info endpoint.
 *
 * This enables us to generate new build info without invalidating caches.
 */
@Configuration
class BuildInfoConfig {
    @Bean
    @ConditionalOnProperty("build.info")
    fun buildProperties(@Value("\${build.info:#{null}}") info: String?) =
        info?.let { BuildProperties(loadFrom(it, "build.")) }

    @Bean
    @ConditionalOnProperty("git.info")
    fun gitProperties(@Value("\${git.info:#{null}}") info: String?) =
        info?.let { GitProperties(loadFrom(it, "git.")) }

    private fun loadFrom(base64Properties: String, prefix: String) = Properties()
        .apply { load(String(Base64.getDecoder().decode(base64Properties)).reader()) }
        .mapKeys { it.key.toString().removePrefix(prefix) }
        .let { Properties().apply { putAll(it) } }
}