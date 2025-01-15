package uk.gov.justice.digital.hmpps.integrations.delius.documents.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(

    @Column(name = "appearance_date")
    val date: ZonedDateTime,

    @Column
    val courtId: Long,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event?,

    @ManyToOne
    @JoinColumn(name = "outcome_id")
    val outcome: ReferenceData?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "court_appearance_id")
    val id: Long,
)
