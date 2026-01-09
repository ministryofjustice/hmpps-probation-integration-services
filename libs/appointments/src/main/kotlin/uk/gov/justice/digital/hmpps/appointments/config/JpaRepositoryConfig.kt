package uk.gov.justice.digital.hmpps.appointments.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps"], considerNestedRepositories = true)
class JpaRepositoryConfig