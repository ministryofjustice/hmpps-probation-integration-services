package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationEvent
import uk.gov.justice.digital.hmpps.listener.AllocationMessage
import java.time.ZonedDateTime

object ResourceLoader {

    private val MAPPER = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(SimpleModule().addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer()))

    fun allocationMessage(filename: String): AllocationEvent =
        MAPPER.readValue(
            MAPPER.readValue(
                ResourceUtils.getFile("classpath:messages/$filename.json"),
                AllocationMessage::class.java
            ).message,
            AllocationEvent::class.java
        )

    fun allocationBody(filename: String): AllocationDetail =
        MAPPER.readValue(
            ResourceUtils.getFile("classpath:simulations/__files/$filename.json"),
            AllocationDetail::class.java
        )
}
