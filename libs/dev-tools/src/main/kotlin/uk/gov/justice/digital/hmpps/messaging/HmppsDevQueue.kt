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
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock

abstract class NotificationChannel(
    val name: String
) {
    private val lock = ReentrantLock()
    private val messages: Queue<Notification<*>> = LinkedList()
    private val processing: MutableMap<UUID, Notification<*>> = HashMap()
    fun publish(notification: Notification<*>) {
        lock.lock()
        try {
            messages.add(notification)
        } finally {
            lock.unlock()
        }
    }

    fun receive(): Notification<*>? {
        lock.lock()
        val notification = try {
            val peek = messages.peek()
            val notification = peek?.let {
                processing[it.id] = it
                messages.poll()
            }
            notification
        } catch (ignore: InterruptedException) {
            null
        } finally {
            lock.unlock()
        }
        return notification
    }

    fun done(notificationId: UUID) {
        lock.lock()
        try {
            processing.remove(notificationId)
        } finally {
            lock.unlock()
        }
    }

    fun publishAndWait(notification: Notification<*>, timeout: Duration = Duration.ofSeconds(20)) {
        publish(notification)
        val start = LocalDateTime.now()
        val end = start.plus(timeout)
        while (messages.any { it.id == notification.id } || processing.containsKey(notification.id)) {
            if (end.isBefore(LocalDateTime.now())) throw TimeoutException("Took too long to process")
            TimeUnit.MILLISECONDS.sleep(100)
        }
    }

    fun pollFor(
        numberOfMessages: Int,
        duration: Duration = Duration.ofSeconds(30),
        interval: Duration = Duration.ofMillis(500)
    ): List<Notification<*>> {
        val maxTime = LocalTime.now().plus(duration)
        val notifications: MutableList<Notification<*>> = mutableListOf()
        while (notifications.size < numberOfMessages && !LocalTime.now().isAfter(maxTime)) {
            val notification = receive()
            if (notification != null) {
                notifications.add(notification)
            }
            TimeUnit.MILLISECONDS.sleep(interval.toMillis())
        }
        check(notifications.size == numberOfMessages) { "Unable to find the correct number of messages in time" }
        return notifications
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
            } finally {
                queue.done(notification.id)
            }
        }
    }
}

@Component
@ConditionalOnProperty("messaging.producer.topic")
class TopicPublisher(
    @Value("\${messaging.producer.topic}") private val topicName: String,
    private val channelManager: HmppsChannelManager
) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        channelManager.getChannel(topicName).publish(notification)
    }
}

@Component
@ConditionalOnProperty("messaging.producer.queue")
class QueuePublisher(
    @Value("\${messaging.producer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager
) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        channelManager.getChannel(queueName).publish(notification)
    }
}

@Configuration
@EnableScheduling
class PollEnableConfig
