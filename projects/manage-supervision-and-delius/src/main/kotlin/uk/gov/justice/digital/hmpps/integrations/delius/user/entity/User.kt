package uk.gov.justice.digital.hmpps.integrations.delius.user.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff

@Entity
@Immutable
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @Column(name = "distinguished_name")
    val username: String,

    @Column
    val forename: String,

    @Column
    val surname: String,
)

interface UserRepository : JpaRepository<User, Long> {

    @Query(
        """
        select u
        from User u
        join fetch u.staff s
        join fetch s.provider p
        where upper(u.username) = upper(:username)
    """
    )
    fun findByUsername(username: String): User?

    @Query("select u from User u where upper(u.username) = upper(:username)")
    fun findUserByUsername(username: String): User?
}

fun UserRepository.getUser(username: String) =
    findByUsername(username) ?: throw NotFoundException("User", "username", username)

fun UserRepository.getUserByUsername(username: String) =
    findUserByUsername(username) ?: throw NotFoundException("User", "username", username)

interface ContactTypeDetails {
    val code: String
    val description: String
}

enum class CaseloadOrderType(val sortColumn: String) {
    NEXT_CONTACT("next_appointment_date_time"),
    LAST_CONTACT("prev_appointment_date_time"),
    SENTENCE("latest_sentence_type_description"),
    SURNAME("surname"),
    NAME_OR_CRN("surname"),
    DOB("date_of_birth")
}
