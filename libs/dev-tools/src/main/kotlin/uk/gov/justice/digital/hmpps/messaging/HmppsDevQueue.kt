package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

abstract class NotificationChannel(
    val name: String
) {
    private val messages: BlockingQueue<Notification<*>> = LinkedBlockingQueue()
    private val processing: MutableMap<UUID, Notification<*>> = ConcurrentHashMap()
    fun publish(notification: Notification<*>) {
        messages.put(notification)
    }

    fun receive(): Notification<*>? {
        val notification = try {
            messages.poll(30, TimeUnit.SECONDS)
        } catch (ignore: InterruptedException) {
            null
        }
        notification?.let { processing[it.id] = it }
        return notification
    }

    fun confirm(notificationId: UUID) {
        processing.remove(notificationId)
    }

    fun fail(notificationId: UUID) {
        processing.remove(notificationId)?.let {
            messages.put(it)
        }
    }

    fun publishAndWait(notification: Notification<*>, timeout: Duration = Duration.ofSeconds(10)) {
        publish(notification)
        val start = LocalDateTime.now()
        val end = start.plus(timeout)
        while (messages.any { it.id == notification.id } || processing.containsKey(notification.id)) {
            if (end.isBefore(LocalDateTime.now())) throw TimeoutException("Took too long to process")
            TimeUnit.MILLISECONDS.sleep(100)
        }
    }
}

@Component
@ConditionalOnProperty("messaging.consumer.queue")
class HmppsNotificationQueue(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String
) : NotificationChannel(queueName)

@Component
@ConditionalOnProperty("messaging.producer.topic")
class HmppsNotificationTopic(
    @Value("\${messaging.producer.topic}")
    private val queueName: String
) : NotificationChannel(queueName)

@Component
class HmppsChannelManager(
    queues: List<NotificationChannel>
) {
    private val queues = queues.associateBy { it.name }
    fun getChannel(name: String): NotificationChannel =
        queues[name] ?: throw IllegalArgumentException("No queue registered with name $name")
}

@Component
@ConditionalOnProperty("messaging.consumer.queue")
class HmppsNotificationListener(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val objectMapper: ObjectMapper,
    private val handler: NotificationHandler<*>
) {

    @Scheduled(fixedDelay = 100)
    fun receive() {
        val queue = channelManager.getChannel(queueName)
        val notification = queue.receive()
        notification?.let {
            val toHandle = Notification(objectMapper.writeValueAsString(it.message), it.attributes, it.id)
            try {
                handler.handle(objectMapper.writeValueAsString(toHandle))
                queue.confirm(notification.id)
            } catch (e: Exception) {
                queue.fail(notification.id)
                throw e
            }
        }
    }
}

@Component
@ConditionalOnProperty("messaging.producer.topic")
class HmppsNotificationPublisher(
    @Value("\${messaging.producer.topic}") private val topicName: String,
    private val channelManager: HmppsChannelManager
) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        channelManager.getChannel(topicName).publish(notification)
    }
}

@Configuration
@EnableScheduling
class PollEnableConfig
