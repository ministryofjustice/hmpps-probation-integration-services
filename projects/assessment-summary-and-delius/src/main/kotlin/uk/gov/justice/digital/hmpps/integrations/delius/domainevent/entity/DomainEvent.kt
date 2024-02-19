package uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.ZonedDateTime

@Entity
@SequenceGenerator(name = "domain_event_id_seq", sequenceName = "domain_event_id_seq", allocationSize = 1)
class DomainEvent(

    @ManyToOne
    @JoinColumn(name = "domain_event_type_id")
    val type: ReferenceData,

    @Column(columnDefinition = "varchar2(4000)")
    val messageBody: String,

    @Column(columnDefinition = "varchar2(4000)")
    val messageAttributes: String,

    @Id
    @Column(name = "domain_event_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "domain_event_id_seq")
    val id: Long = 0
) {
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()
}

interface DomainEventRepository : JpaRepository<DomainEvent, Long>
