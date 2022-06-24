package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

@Immutable
@Entity(name = "audited_interaction")
class AuditedInteraction(

    @Id
    val dateTime: ZonedDateTime = ZonedDateTime.now(),

    val businessInteractionId: Long,
    val userId: Long,

    @Convert(converter = AuditedInteractionOutcomeConverter::class)
    val outcome: Outcome = Outcome.SUCCESS,

    @Column(name = "interaction_parameters", length = 500)
    @Convert(converter = AuditedInteractionOutcomeConverter::class)
    val parameters: Parameter = Parameter(),

) {
    enum class Outcome {
        SUCCESS, FAIL
    }

    class Parameter
}
