package uk.gov.justice.digital.hmpps.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.model.Provider

@Configuration
@EnableConfigurationProperties(JobConfig::class)
@ConditionalOnProperty(prefix = "job", name = ["name"])
class EnableJobConfig

@ConfigurationProperties(prefix = "job")
data class JobConfig(
    val name: String,
    val daysInAdvance: Int,
    val provider: Provider,
    val templates: List<String>,
    val trials: List<TrialConfig> = emptyList(),
)

data class TrialConfig(
    val templates: List<String>,
)