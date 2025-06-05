package uk.gov.justice.digital.hmpps.integrations.delius.user.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.io.Serializable
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffUser

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
    val user: StaffUser,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider
) : Serializable

interface ProbationAreaUserRepository : JpaRepository<ProbationAreaUser, Long> {
    @Query("""
        SELECT pau FROM ProbationAreaUser pau 
        JOIN FETCH pau.id.provider p
        WHERE UPPER(pau.id.user.username) = UPPER(:username)
        AND p.selectable IS TRUE 
        """
    )
    fun findByUsername(username: String): List<ProbationAreaUser>
}