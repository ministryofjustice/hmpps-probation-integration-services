package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "user_")
@SQLRestriction("end_date is null or end_date > current_date")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "distinguished_name")
    val username: String,

    @Column(name = "surname")
    val surname: String,

    @Column
    val endDate: LocalDate? = null
)

interface UserRepository : JpaRepository<User, Long>
