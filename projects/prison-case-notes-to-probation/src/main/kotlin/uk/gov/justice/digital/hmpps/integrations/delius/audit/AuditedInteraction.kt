package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.hibernate.annotations.Immutable
import org.springframework.data.domain.Persistable
import uk.gov.justice.digital.hmpps.integrations.delius.audit.converter.AuditedInteractionOutcomeConverter
import uk.gov.justice.digital.hmpps.integrations.delius.audit.converter.AuditedInteractionParamsConverter
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

class AuditedInteractionId(

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
        SUCCESS, FAIL
    }

    data class Parameters(private val paramMap: Map<String, String> = mapOf()) {

        constructor(vararg paramPairs: Pair<String, String>) : this(paramPairs.toMap())

        fun paramPairs(): List<Pair<String, String>> = paramMap.entries.map { Pair(it.key, it.value) }
    }

    override fun getId() = AuditedInteractionId(dateTime, businessInteractionId, userId)

    override fun isNew() = true
}
