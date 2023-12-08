package uk.gov.justice.digital.hmpps.integrations.delius.location

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,
    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findByCode(code: String): OfficeLocation?
}
