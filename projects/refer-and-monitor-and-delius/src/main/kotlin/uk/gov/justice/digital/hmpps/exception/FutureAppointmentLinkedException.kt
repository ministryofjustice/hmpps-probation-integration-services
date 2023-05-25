package uk.gov.justice.digital.hmpps.exception

class FutureAppointmentLinkedException :
    RuntimeException("Unable to delete a future appointment as it is linked to another contact")
