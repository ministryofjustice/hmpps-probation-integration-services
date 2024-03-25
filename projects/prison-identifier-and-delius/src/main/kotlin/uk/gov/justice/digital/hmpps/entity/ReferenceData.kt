package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,

    @Column(name = "code_value")
    val code: String,

    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val set: ReferenceDataSet,
) {
    fun prisonGenderCode() = when (code) {
        "M", "F" -> code
        "N" -> "NK"
        else -> "ALL"
    }
}

@Entity
@Immutable
@Table(name = "r_reference_data_master")
class ReferenceDataSet(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Column(name = "code_set_name")
    val name: String
)

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCodeAndSetName(code: String, setName: String): ReferenceData?
}

fun ReferenceDataRepository.getByCodeAndSetName(code: String, set: String): ReferenceData =
    findByCodeAndSetName(code, set) ?: throw NotFoundException(set, "code", code)

fun ReferenceDataRepository.duplicateNomsNumberIdentifierType(): ReferenceData =
    getByCodeAndSetName("DNOMS", "ADDITIONAL IDENTIFIER TYPE")

fun ReferenceDataRepository.formerNomsNumberIdentifierType(): ReferenceData =
    getByCodeAndSetName("XNOMS", "ADDITIONAL IDENTIFIER TYPE")
