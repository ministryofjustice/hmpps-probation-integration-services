package uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Table(name = "probation_area")
@Entity(name = "UserDetailsProvider")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @Column
    val description: String
)

interface UserDetailsProviderRepository : JpaRepository<Provider, Long> {
    fun findByCode(code: String): Provider?
}
