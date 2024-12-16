package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
data class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "appearance_date")
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: Court,

    @ManyToOne
    @JoinColumn(name = "appearance_type_id")
    val type: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "plea_id")
    val plea: ReferenceData?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Entity
@Immutable
data class Court(
    @Id
    @Column(name = "court_id")
    val id: Long,

    @Column(name = "court_name")
    val name: String
)
