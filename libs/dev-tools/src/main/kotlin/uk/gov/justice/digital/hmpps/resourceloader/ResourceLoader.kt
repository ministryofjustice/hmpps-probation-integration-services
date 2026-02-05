package uk.gov.justice.digital.hmpps.resourceloader

import org.springframework.util.ResourceUtils
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import java.time.ZonedDateTime

object ResourceLoader {
    val MAPPER: ObjectMapper = jsonMapper {
        addModule(kotlinModule())
        addModule(SimpleModule().addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer()))
    }

    fun event(filename: String): HmppsDomainEvent = get(filename)

    inline fun <reified T> get(filename: String): T =
        MAPPER.readValue<T>(ResourceUtils.getFile("classpath:messages/$filename.json"))

    inline fun <reified T> message(filename: String): T = MAPPER.readValue(get<Notification<String>>(filename).message)

    inline fun <reified T> notification(filename: String): Notification<T> {
        val stringMessage = get<Notification<String>>(filename)
        return Notification(
            message = MAPPER.readValue(stringMessage.message, T::class.java),
            attributes = stringMessage.attributes
        )
    }

    inline fun <reified T> file(filename: String): T =
        MAPPER.readValue(
            ResourceUtils.getFile("classpath:simulations/__files/$filename.json")
        )
}
