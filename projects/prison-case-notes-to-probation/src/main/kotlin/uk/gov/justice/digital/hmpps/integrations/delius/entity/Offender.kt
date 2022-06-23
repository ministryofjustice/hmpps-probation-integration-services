package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Immutable
@Entity(name = "offender")
class Offender(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(name = "noms_number")
    val nomsId: String
)
