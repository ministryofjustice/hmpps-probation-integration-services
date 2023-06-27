package uk.gov.justice.digital.hmpps.service.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.service.custody.Custody

@Immutable
@Entity
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    var custody: Custody? = null,

    @Column(name = "active_flag", columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)
