package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.annotations.Immutable
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Immutable
@Entity
class Nsi(
    @Id
    @Column(name = "nsi_id")
    var id: Long,

    val referralDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "nsi_outcome_id", updatable = false)
    val outcome: StandardReferenceList? = null,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id", updatable = false)
    val status: StandardReferenceList,

    @ManyToOne
    @JoinColumn(name = "event_id", updatable = false)
    val event: Event? = null,

    @Column(name = "active_flag", updatable = false, columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "NUMBER")
    var softDeleted: Boolean = false,
)

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class StandardReferenceList(
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,
)

@Immutable
@Entity
@Table(name = "event")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,
)
