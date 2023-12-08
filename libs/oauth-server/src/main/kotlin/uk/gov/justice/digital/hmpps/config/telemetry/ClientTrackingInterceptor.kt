package uk.gov.justice.digital.hmpps.config.telemetry

import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ClientTrackingConfiguration(private val clientTrackingInterceptor: ClientTrackingInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
    }
}

@Component
class ClientTrackingInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        request.getHeader(HttpHeaders.AUTHORIZATION)?.let {
            try {
                val jwtBody = SignedJWT.parse(it.replace(BEARER, "")).jwtClaimsSet
                Span.current().setAttribute("clientId", jwtBody.getClaim("client_id").toString())
            } catch (ignored: Exception) {
                // Do nothing - don't create client id span
            }
        }
        return true
    }

    companion object {
        private const val BEARER = "Bearer "
    }
}
