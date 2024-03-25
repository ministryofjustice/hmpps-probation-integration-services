package uk.gov.justice.digital.hmpps.config

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import java.util.*

class BuildInfoConfigTest {
    @Test
    fun `converts base64-encoded properties into BuildProperties`() {
        val properties = """
            build.artifact=value1
            build.group    =  value2
            build.name=value3\nvalue3
            #build.version=value4
            
            non-build.other=value5
        """.trimIndent()
        val encodedProperties = Base64.getEncoder().encodeToString(properties.toByteArray())
        val buildProperties = BuildInfoConfig().buildProperties(encodedProperties)

        checkNotNull(buildProperties)
        assertThat(buildProperties.artifact, equalTo("value1"))
        assertThat(buildProperties.group, equalTo("value2"))
        assertThat(buildProperties.name, equalTo("value3\nvalue3"))
        assertThat(buildProperties.version, nullValue())
    }

    @Test
    fun `converts base64-encoded properties into GitProperties`() {
        val properties = "git.branch=value1"
        val encodedProperties = Base64.getEncoder().encodeToString(properties.toByteArray())
        val gitProperties = BuildInfoConfig().gitProperties(encodedProperties)

        checkNotNull(gitProperties)
        assertThat(gitProperties.branch, equalTo("value1"))
    }

    @Test
    fun `handles null`() {
        val gitProperties = BuildInfoConfig().gitProperties(null)
        assertThat(gitProperties, nullValue())
    }
}
