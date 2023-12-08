package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity.ProbationArea
import java.io.Serializable

@Immutable
@Entity
@Table(name = "r_institution")
class Institution(
    @EmbeddedId
    val id: InstitutionId,
    @Column(nullable = false, columnDefinition = "char(6)")
    val code: String,
    @Column
    val nomisCdeCode: String?,
    @Column(nullable = false)
    val description: String,
    @OneToOne(mappedBy = "institution")
    val probationArea: ProbationArea? = null,
    @Column(name = "immigration_removal_centre")
    @Convert(converter = YesNoConverter::class)
    val irc: Boolean? = null,
    @Column(name = "secure_hospital")
    @Convert(converter = YesNoConverter::class)
    val secureHospital: Boolean? = null,
)

@Embeddable
data class InstitutionId(
    @Column(name = "institution_id")
    val institutionId: Long,
    @Column(name = "establishment")
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,
) : Serializable

interface InstitutionRepository : JpaRepository<Institution, InstitutionId> {
    fun findByNomisCdeCode(code: String): Institution?

    fun findByCode(code: String): Institution?
}

fun InstitutionRepository.getByNomisCdeCode(code: String): Institution =
    findByNomisCdeCode(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)

fun InstitutionRepository.getByCode(code: String): Institution =
    findByCode(code) ?: throw NotFoundException("Institution", "code", code)
