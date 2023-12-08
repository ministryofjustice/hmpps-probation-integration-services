package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
    @Column(columnDefinition = "char(3)")
    val code: String,
    val description: String,
)

interface ProviderRepository : JpaRepository<Provider, Long> {
    fun findByCode(code: String): Provider
}
