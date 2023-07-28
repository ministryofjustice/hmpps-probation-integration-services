resource "aws_sns_topic_subscription" "SERVICE_NAME-queue-subscription" {
  topic_arn = data.aws_sns_topic.hmpps-domain-events.arn
  protocol  = "sqs"
  endpoint  = module.SERVICE_NAME-queue.sqs_arn
  filter_policy = jsonencode({
    eventType = [] # TODO add event type filter
  })
}

module "SERVICE_NAME-queue" {
  source                 = "github.com/ministryofjustice/cloud-platform-terraform-sqs?ref=4.11.0"
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
  source                 = "github.com/ministryofjustice/cloud-platform-terraform-sqs?ref=4.11.0"
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
    QUEUE_NAME = module.SERVICE_NAME-queue.sqs_name
  }
}

module "service_account" {
  source                 = "github.com/ministryofjustice/cloud-platform-terraform-irsa?ref=2.0.0"
  application            = var.application
  business_unit          = var.business_unit
  eks_cluster_name       = var.eks_cluster_name
  environment_name       = var.environment_name
  infrastructure_support = var.infrastructure_support
  is_production          = var.is_production
  namespace              = var.namespace
  team_name              = var.team_name

  service_account_name = "SERVICE_NAME"
  role_policy_arns     = { sqs = module.SERVICE_NAME-queue.irsa_policy_arn }
}
