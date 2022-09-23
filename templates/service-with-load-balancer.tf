module "$SERVICE_NAME" {
  source                   = "../../modules/ecs_service"
  region                   = var.region
  environment_name         = var.environment_name
  short_environment_name   = var.short_environment_name
  remote_state_bucket_name = var.remote_state_bucket_name
  tags                     = var.tags

  # Application Container
  service_name                   = "$SERVICE_NAME"
  health_check_path              = "/health"
  ignore_task_definition_changes = true

  # Security & Networking
  task_role_arn      = aws_iam_role.ecs_sqs_task.arn
  target_group_count = 1
  security_groups = [
    aws_security_group.$SERVICE_NAME-instances.id,
    data.terraform_remote_state.delius_core_security_groups.outputs.sg_common_out_id,
    data.terraform_remote_state.delius_core_security_groups.outputs.sg_delius_db_access_id,
  ]

  # Monitoring
  notification_arn = data.terraform_remote_state.alerts.outputs.aws_sns_topic_alarm_notification_arn

  # Scaling
  min_capacity = local.min_capacity
  max_capacity = local.max_capacity
}

resource "aws_iam_role_policy_attachment" "$SERVICE_NAME" {
  role       = module.$SERVICE_NAME.exec_role.name
  policy_arn = aws_iam_policy.access_ssm_parameters.arn
}

resource "random_id" "$SERVICE_NAME" {
  byte_length = 8
}

resource "aws_lb" "$SERVICE_NAME" {
  internal = false
  subnets = [
    data.terraform_remote_state.vpc.outputs.vpc_public-subnet-az1,
    data.terraform_remote_state.vpc.outputs.vpc_public-subnet-az2,
    data.terraform_remote_state.vpc.outputs.vpc_public-subnet-az3
  ]
  security_groups = [aws_security_group.$SERVICE_NAME-lb.id]
  tags            = merge(var.tags, { Name = "${var.short_environment_name}-$SERVICE_NAME-lb" })

  access_logs {
    enabled = true
    bucket  = data.terraform_remote_state.access_logs.outputs.bucket_name
    prefix  = "$SERVICE_NAME"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_lb_listener" "$SERVICE_NAME" {
  load_balancer_arn = aws_lb.$SERVICE_NAME.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = local.certificate_arn
  default_action {
    type = "forward"
    forward {
      target_group {
        arn = module.$SERVICE_NAME.primary_target_group["arn"]
      }
    }
  }
}

resource "aws_route53_record" "$SERVICE_NAME" {
  zone_id = local.route53_zone_id
  name    = "$SERVICE_NAME"
  type    = "CNAME"
  ttl     = 300
  records = [aws_lb.$SERVICE_NAME.dns_name]
}

resource "aws_security_group" "$SERVICE_NAME-lb" {
  name        = "${var.environment_name}-$SERVICE_NAME-lb"
  vpc_id      = data.terraform_remote_state.vpc.outputs.vpc_id
  description = "$SERVICE_NAME load balancer"
  tags        = merge(var.tags, { Name = "${var.environment_name}-$SERVICE_NAME-lb" })
  lifecycle {
    create_before_destroy = true
  }
  ingress {
    from_port   = 443
    protocol    = "tcp"
    to_port     = 443
    cidr_blocks = concat(var.internal_moj_access_cidr_blocks, local.bastion_public_ip, local.natgateway_public_ips_cidr_blocks)
    description = "Ingress from VPNs and Bastion hosts"
  }
  ingress {
    from_port   = 443
    protocol    = "tcp"
    to_port     = 443
    cidr_blocks = var.moj_cloud_platform_cidr_blocks
    description = "Ingress from MOJ Cloud Platform"
  }
  egress {
    from_port       = 8080
    protocol        = "tcp"
    to_port         = 8080
    security_groups = [aws_security_group.$SERVICE_NAME-instances.id]
    description     = "Egress to instances"
  }
}

resource "aws_security_group" "$SERVICE_NAME-instances" {
  name        = "${var.environment_name}-$SERVICE_NAME-instances"
  vpc_id      = data.terraform_remote_state.vpc.outputs.vpc_id
  description = "$SERVICE_NAME instances"
  tags        = merge(var.tags, { Name = "${var.environment_name}-$SERVICE_NAME-instances" })
  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "$SERVICE_NAME" {
  security_group_id        = aws_security_group.$SERVICE_NAME-instances.id
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = 8080
  to_port                  = 8080
  source_security_group_id = aws_security_group.$SERVICE_NAME-lb.id
  description              = "Ingress from load balancer"
}

output "$SERVICE_NAME" {
  value = {
    url = "https://${aws_route53_record.$SERVICE_NAME.fqdn}"
  }
}
