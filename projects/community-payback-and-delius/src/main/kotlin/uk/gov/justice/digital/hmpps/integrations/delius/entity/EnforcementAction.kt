package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Table(name = "r_enforcement_action")
@Immutable
class EnforcementAction(
    @Id
    @Column(name = "enforcement_action_id")
    val id: Long,

    val code: String,

    val description: String,

    val responseByPeriod: Long?,

    @Convert(converter = YesNoConverter::class)
    val outstandingContactAction: Boolean?,

    val contactTypeId: Long,
) {
    companion object {
        const val REFER_TO_PERSON_MANAGER = "ROM"
    }
}

interface EnforcementActionRepository : JpaRepository<EnforcementAction, Long> {
    fun findEnforcementActionByCode(code: String): EnforcementAction?
}

fun EnforcementActionRepository.getEnforcementAction(code: String) =
    findEnforcementActionByCode(code) ?: throw NotFoundException("Enforcement Action", "code", code)