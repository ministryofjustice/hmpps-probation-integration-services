package uk.gov.justice.digital.hmpps.data.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    var userId: Long,

    @Column(name = "distinguished_name")
    var username: String,
)

interface UserRepository : JpaRepository<User, Long>
