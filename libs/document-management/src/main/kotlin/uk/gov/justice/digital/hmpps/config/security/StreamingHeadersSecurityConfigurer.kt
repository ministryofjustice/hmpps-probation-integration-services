package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.ObjectPostProcessor
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.header.HeaderWriterFilter

/**
 * Workaround for Spring issue with streaming responses:
 *  * https://github.com/spring-projects/spring-security/issues/9175
 *  * https://github.com/spring-projects/spring-framework/issues/31543
 */
@Configuration
class StreamingHeadersSecurityConfigurer : SecurityConfigurer {
    override fun configure(http: HttpSecurity): HttpSecurity {
        http.headers {
            it.withObjectPostProcessor(object : ObjectPostProcessor<HeaderWriterFilter> {
                override fun <T : HeaderWriterFilter> postProcess(filter: T) = filter.also {
                    filter.setShouldWriteHeadersEagerly(true)
                }
            })
        }
        return http
    }
}
