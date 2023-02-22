
resource "pingdom_check" "$SERVICE_NAME" {
  type                     = "http"
  name                     = "Integration - $SERVICE_NAME"
  host                     = "health-kick.prison.service.justice.gov.uk"
  port                     = 443
  url                      = "/https/$SERVICE_URL"
  resolution               = 1
  notifywhenbackup         = true
  sendnotificationwhendown = 6
  notifyagainevery         = 0
  encryption               = true
  tags                     = "probation-integration,hmpps,cloudplatform-managed"
  probefilters             = "region:EU"
  integrationids           = [120233] # probation-integration-notifications
}
