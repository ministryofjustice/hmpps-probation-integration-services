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
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
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
    private var httpServletRequest: HttpServletRequest,
    private val aur: AuditUserRepository
) {

    // Wraps controller with db call pkg_vpd_ctx.set_client_identifier so that web application logged-in username is used
    // when updating the user field in notes. Without this wrapper, service name is used in notes.
    @Before("@annotation(uk.gov.justice.digital.hmpps.aspect.WithDeliusUser)")
    fun beforeRequest() {
        getDeliusUsername()?.let { deliusUserName ->
            aur.findUserByUsername(deliusUserName)?.also {
                userContext.set(UserContext(it.username, it.id))
            }
            namedParameterJdbcTemplate.update(
                "call pkg_vpd_ctx.set_client_identifier(:dbName)",
                MapSqlParameterSource().addValue("dbName", deliusUserName)
            )
        }
    }

    @After("@annotation(uk.gov.justice.digital.hmpps.aspect.WithDeliusUser)")
    fun afterRequest() {
        userContext.set(null)
        getDeliusUsername()?.let {
            jdbcTemplate.execute("call pkg_vpd_ctx.clear_client_identifier()")
        }
    }

    fun getDeliusUsername(): String? {
        return try {
            httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)?.let {
                val claims = SignedJWT.parse(it.replace("Bearer ", "")).jwtClaimsSet?.claims
                if (claims?.containsKey("user_name") == true) claims["user_name"] as? String else null
            }
        } catch (ignored: Exception) {
            null
        }
    }

    companion object {
        internal val userContext: ThreadLocal<UserContext?> = ThreadLocal<UserContext?>()
    }
}

data class UserContext(val username: String, val userId: Long) {
    companion object {
        fun get(): UserContext? = DeliusUserAspect.userContext.get()
    }
}
