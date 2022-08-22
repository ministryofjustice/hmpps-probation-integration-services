package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.hibernate.annotations.Where
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.release.Release
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "soft_deleted = 0")
class Recall(
    @Id
    @SequenceGenerator(name = "recall_id_generator", sequenceName = "recall_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recall_id_generator")
    @Column(name = "recall_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(name = "recall_date")
    val date: ZonedDateTime,

    @OneToOne
    @JoinColumn(name = "release_id", nullable = false)
    val release: Release,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
)
