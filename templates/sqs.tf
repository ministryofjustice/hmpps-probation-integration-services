resource "aws_sns_topic_subscription" "SERVICE_NAME-queue-subscription" {
  topic_arn = data.aws_sns_topic.hmpps-domain-events.arn
  protocol  = "sqs"
  endpoint  = module.SERVICE_NAME-queue.sqs_arn
  filter_policy = jsonencode({
    eventType = [] # TODO add event type filter
  })
}

module "SERVICE_NAME-queue" {
  source                 = "github.com/ministryofjustice/cloud-platform-terraform-sqs?ref=4.10.1"
  namespace              = var.namespace
  team_name              = var.team_name
  environment-name       = var.environment_name
  infrastructure-support = var.infrastructure_support

  application = "SERVICE_NAME"
  sqs_name    = "SERVICE_NAME-queue"

  redrive_policy = jsonencode({
    deadLetterTargetArn = module.SERVICE_NAME-dlq.sqs_arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue_policy" "SERVICE_NAME-queue-policy" {
  queue_url = module.SERVICE_NAME-queue.sqs_id
  policy    = data.aws_iam_policy_document.sqs_queue_policy_document.json
}

module "SERVICE_NAME-dlq" {
  source                 = "github.com/ministryofjustice/cloud-platform-terraform-sqs?ref=4.10.0"
  namespace              = var.namespace
  team_name              = var.team_name
  environment-name       = var.environment_name
  infrastructure-support = var.infrastructure_support

  application = "SERVICE_NAME"
  sqs_name    = "SERVICE_NAME-dlq"
}

resource "aws_sqs_queue_policy" "SERVICE_NAME-dlq-policy" {
  queue_url = module.SERVICE_NAME-dlq.sqs_id
  policy    = data.aws_iam_policy_document.sqs_queue_policy_document.json
}

resource "kubernetes_secret" "SERVICE_NAME-queue-secret" {
  metadata {
    name      = "SERVICE_NAME-queue"
    namespace = var.namespace
  }
  data = {
    QUEUE_NAME            = module.SERVICE_NAME-queue.sqs_name
    AWS_ACCESS_KEY_ID     = module.SERVICE_NAME-queue.access_key_id
    AWS_SECRET_ACCESS_KEY = module.SERVICE_NAME-queue.secret_access_key
  }
}