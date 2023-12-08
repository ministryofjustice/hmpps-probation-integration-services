package uk.gov.justice.digital.hmpps.audit.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import org.hibernate.annotations.Immutable
import org.springframework.data.domain.Persistable
import uk.gov.justice.digital.hmpps.audit.converter.AuditedInteractionOutcomeConverter
import uk.gov.justice.digital.hmpps.audit.converter.AuditedInteractionParamsConverter
import java.io.Serializable
import java.time.ZonedDateTime

data class AuditedInteractionId(
    @Column(columnDefinition = "timestamp")
    val dateTime: ZonedDateTime = ZonedDateTime.now(),
    val businessInteractionId: Long = 0,
    val userId: Long = 0,
) : Serializable {
    companion object {
        private const val serialVersionUID = -671767078136505398L
    }
}

@Immutable
@Entity(name = "audited_interaction")
@IdClass(AuditedInteractionId::class)
class AuditedInteraction(
    @Id
    val businessInteractionId: Long,
    @Id
    val userId: Long,
    @Id
    val dateTime: ZonedDateTime = ZonedDateTime.now(),
    @Convert(converter = AuditedInteractionOutcomeConverter::class)
    val outcome: Outcome = Outcome.SUCCESS,
    @Column(name = "interaction_parameters", length = 500)
    @Convert(converter = AuditedInteractionParamsConverter::class)
    val parameters: Parameters = Parameters(),
) : Persistable<AuditedInteractionId> {
    enum class Outcome {
        SUCCESS,
        FAIL,
    }

    data class Parameters(private val paramMap: MutableMap<String, Any> = mutableMapOf()) {
        constructor(vararg paramPairs: Pair<String, Any>) : this(paramPairs.toMap().toMutableMap())

        fun paramPairs(): List<Pair<String, Any>> = paramMap.entries.map { Pair(it.key, it.value) }

        operator fun get(key: String): Any? = paramMap[key]

        operator fun set(
            key: String,
            value: Any,
        ) {
            paramMap[key] = value
        }
    }

    override fun getId() = AuditedInteractionId(dateTime, businessInteractionId, userId)

    override fun isNew() = true
}
