package uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.jpa.GeneratedId

@Entity
@SequenceGenerator(
    name = "domain_event_id_seq",
    sequenceName = "domain_event_id_seq",
    allocationSize = 1
)
class DomainEvent(

    @Id
    @GeneratedId(generator = "domain_event_id_seq")
    @Column(name = "domain_event_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "domain_event_type_id", nullable = false)
    val type: ReferenceData,

    @Column(name = "message_body", columnDefinition = "varchar2(4000)", nullable = false)
    val messageBody: String,

    @Column(name = "message_attributes", columnDefinition = "varchar2(4000)", nullable = false)
    val messageAttributes: String
)

interface DomainEventRepository : JpaRepository<DomainEvent, Long>
