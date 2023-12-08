package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Immutable
@Entity
class Offender(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String,
    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
)
