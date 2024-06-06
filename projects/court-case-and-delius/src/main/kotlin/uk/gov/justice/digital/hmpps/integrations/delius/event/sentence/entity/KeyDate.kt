package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
class KeyDate(

    @Id
    @Column(name = "key_date_id")
    val id: Long,

    val keyDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val keyDateType: ReferenceData
)