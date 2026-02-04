package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable

@Entity
@Immutable
@Table(name = "r_con_type_req_type_maincat")
class ContactTypeRequirementType(
    @Id
    val id: ContactTypeRequirementTypeId
)

@Embeddable
class ContactTypeRequirementTypeId(
    @Column(name = "contact_type_id")
    val contactTypeId: Long,

    @Column(name = "rqmnt_type_main_category_id")
    val requirementTypeId: Long
) : Serializable

interface ContactTypeRequirementTypeRepository :
    JpaRepository<ContactTypeRequirementType, ContactTypeRequirementTypeId> {
    fun findByIdContactTypeId(contactTypeId: Long): List<ContactTypeRequirementType>
}