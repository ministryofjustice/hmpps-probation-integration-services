package uk.gov.justice.digital.hmpps.integrations.delius.contact.alert

import org.springframework.data.jpa.repository.JpaRepository

interface ContactAlertRepository : JpaRepository<ContactAlert, Long>
