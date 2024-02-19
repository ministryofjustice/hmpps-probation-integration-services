package uk.gov.justice.digital.hmpps.integrations.delius.contact.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id", nullable = false)
    val id: Long,

    @Column(nullable = false)
    val code: String
) {
    enum class Code(val value: String) {
        RELEASE_FROM_CUSTODY("EREL"),
        COMPONENT_TERMINATED("ETER"),
        BREACH_PRISON_RECALL("ERCL"),
        PRISON_MANAGER_AUTOMATIC_TRANSFER("EPOMAT"),
        COMPONENT_PROVIDER_TRANSFER_REJECTED("ETCX"),
        CHANGE_OF_INSTITUTION("ETCP"),
        DIED_IN_CUSTODY("DUS3"),
        PSS_BREACH_COMMITTAL_RELEASE("ERPSSR")
    }
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String): ContactType =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)
