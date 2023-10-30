package uk.gov.justice.digital.hmpps.config.telemetry

import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor

@Configuration
class ClientTrackingInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (token.startsWith(BEARER)) {
            try {
                val jwtBody = SignedJWT.parse(token.replace(BEARER, "")).jwtClaimsSet
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
