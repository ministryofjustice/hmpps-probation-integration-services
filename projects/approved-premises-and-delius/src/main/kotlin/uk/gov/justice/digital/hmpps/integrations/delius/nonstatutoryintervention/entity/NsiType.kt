package uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "r_nsi_type")
class NsiType(
    @Id
    @Column(name = "nsi_type_id")
    val id: Long = 0,

    @Column(name = "code")
    val code: String,

    val description: String
)

enum class NsiTypeCode(val code: String) {
    APPROVED_PREMISES_RESIDENCE("APR01")
}

interface NsiTypeRepository : JpaRepository<NsiType, Long> {
    fun findByCode(code: String): NsiType?
}

fun NsiTypeRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("NsiType", "code", code)
