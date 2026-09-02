package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
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

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean
)

interface ProviderRepository : JpaRepository<Provider, Long> {
    fun findByCodeIn(codes: Collection<String>): List<Provider>
}
