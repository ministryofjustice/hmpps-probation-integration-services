package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository

@Entity
class DomainEvent(
    @Id
    @Column(name = "domain_event_id")
    val id: Long,
    @Column(columnDefinition = "varchar2(4000)")
    val messageBody: String,
    @Column(columnDefinition = "varchar2(4000)")
    val messageAttributes: String,
)

interface DomainEventRepository : JpaRepository<DomainEvent, Long>
