package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import java.io.Serializable

@Entity
@Immutable
@Table(name = "probation_area_user")
class ProbationAreaUser(
    @EmbeddedId
    val id: ProbationAreaUserId,
)

@Embeddable
class ProbationAreaUserId(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: ProbationArea
) : Serializable
