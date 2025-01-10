package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
@Table(name = "offender")
class DocPerson(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "CHAR(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
