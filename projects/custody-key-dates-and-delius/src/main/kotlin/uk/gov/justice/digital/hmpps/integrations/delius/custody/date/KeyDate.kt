package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.custody.BaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import java.time.LocalDate

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "key_date_id_seq", sequenceName = "key_date_id_seq", allocationSize = 1)
class KeyDate(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "key_date_id_seq")
    @Column(name = "key_date_id")
    val id: Long?,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody? = null,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate

) : BaseEntity()
