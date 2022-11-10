package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Immutable
class Nsi(
    @Id
    @Column(name = "nsi_id", updatable = false)
    val id: Long = 0,

    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent?,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id", updatable = false)
    val type: NsiType,
)

@Immutable
@Entity
@Table(name = "r_nsi_type")
class NsiType(
    @Id
    @Column(name = "nsi_type_id")
    val id: Long,

    val description: String
)
