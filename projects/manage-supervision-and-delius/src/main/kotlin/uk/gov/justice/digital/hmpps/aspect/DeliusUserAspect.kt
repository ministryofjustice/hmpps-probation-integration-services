package uk.gov.justice.digital.hmpps.aspect

import com.nimbusds.jwt.SignedJWT
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithDeliusUser

@Aspect
@Component
class DeliusUserAspect(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val jdbcTemplate: JdbcTemplate,
    private val httpServletRequest: HttpServletRequest
) {

    @Before("@annotation(uk.gov.justice.digital.hmpps.aspect.WithDeliusUser)")
    fun beforeRequest() {
        getDeliusUsername()?.let { deliusUserName ->
            namedParameterJdbcTemplate.update(
                "call pkg_vpd_ctx.set_client_identifier(:dbName)",
                MapSqlParameterSource().addValue("dbName", deliusUserName)
            )
        }
    }

    @After("@annotation(uk.gov.justice.digital.hmpps.aspect.WithDeliusUser)")
    fun afterRequest() {
        getDeliusUsername()?.let {
            jdbcTemplate.execute("call pkg_vpd_ctx.clear_client_identifier()")
        }
    }

    fun getDeliusUsername(): String? {
        return try {
            httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)?.let {
                val claims = SignedJWT.parse(it.replace("Bearer ", "")).jwtClaimsSet?.claims
                if (claims?.containsKey("user_name") == true) claims["user_name"].toString() else null
            }
        } catch (ignored: Exception) {
            null
        }
    }
}
