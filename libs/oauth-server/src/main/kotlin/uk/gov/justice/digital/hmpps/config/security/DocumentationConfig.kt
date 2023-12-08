package uk.gov.justice.digital.hmpps.config.security

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders

@OpenAPIDefinition(
    info =
        Info(
            title = "API Reference",
            contact =
                Contact(
                    name = "Probation Integration Team",
                    email = "probation-integration-team@digital.justice.gov.uk",
                    // #probation-integration-tech Slack channel
                    url = "https://mojdt.slack.com/archives/C02HQ4M2YQN",
                ),
            license =
                License(
                    name = "MIT",
                    url = "https://github.com/ministryofjustice/hmpps-probation-integration-services/blob/main/LICENSE",
                ),
            version = "1.0",
        ),
    servers = [Server(url = "/")],
    security = [SecurityRequirement(name = "hmpps-auth-token")],
)
@SecurityScheme(
    name = "hmpps-auth-token",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER,
    paramName = HttpHeaders.AUTHORIZATION,
)
@Configuration
class DocumentationConfig
