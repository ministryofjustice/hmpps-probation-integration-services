package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.model.CodeDescription

@Entity
@Immutable
@Table(name = "probation_area_user")
class ProbationAreaUser(
    @EmbeddedId
    val id: ProbationAreaUserId
)

@Embeddable
class ProbationAreaUserId(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider
)

interface ProbationAreaUserRepository : JpaRepository<ProbationAreaUser, Long> {
    @Query(
        """
        select pau from ProbationAreaUser pau 
        join fetch pau.id.provider 
        where upper(pau.id.user.username) = upper(:username) 
        and pau.id.provider.selectable = true
        """
    )
    fun findByUsername(username: String): List<ProbationAreaUser>
}

fun ProbationAreaUser.toProviderCodeDescription() = CodeDescription(
    code = this.id.provider.code,
    description = this.id.provider.description
)
