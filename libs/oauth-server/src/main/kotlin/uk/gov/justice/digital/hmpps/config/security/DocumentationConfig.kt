package uk.gov.justice.digital.hmpps.config.security

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders

@OpenAPIDefinition(
    servers = [Server(url = "/")],
    security = [SecurityRequirement(name = "hmpps-auth-token")],
)
@SecurityScheme(
    name = "hmpps-auth-token",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER,
    paramName = HttpHeaders.AUTHORIZATION
)
@Configuration
class DocumentationConfig
