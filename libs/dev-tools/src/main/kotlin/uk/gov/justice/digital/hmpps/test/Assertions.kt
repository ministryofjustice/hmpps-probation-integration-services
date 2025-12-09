package uk.gov.justice.digital.hmpps.test

import org.assertj.core.api.Assertions.assertThat

object Assertions {
    fun <T> assertNotNull(value: T?): T {
        assertThat(value).isNotNull
        return value!!
    }
}
