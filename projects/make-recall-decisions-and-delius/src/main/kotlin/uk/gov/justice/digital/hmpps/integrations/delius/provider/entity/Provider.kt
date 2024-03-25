package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @Column
    val description: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column
    val endDate: LocalDate? = null
)

interface ProviderRepository : JpaRepository<Provider, Long> {
    fun findByCode(code: String): Provider?

    @Query("select p from Provider p where p.selectable = true and (p.endDate is null or p.endDate > current_date)")
    fun findActive(): List<Provider>
}
