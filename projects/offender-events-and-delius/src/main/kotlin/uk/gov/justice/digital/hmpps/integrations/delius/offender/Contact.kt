package uk.gov.justice.digital.hmpps.integrations.delius.offender

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "contact")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long = 0,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "visor_contact")
    @Convert(converter = YesNoConverter::class)
    val visorContact: Boolean? = false
)

interface ContactRepository : JpaRepository<Contact, Long> {

    fun existsByIdAndSoftDeletedFalse(id: Long): Boolean
}

