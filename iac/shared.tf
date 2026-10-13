#############################################
# Shared infrastructure OWNED BY MOUSESCORING
#
# The VPC, subnets, security groups and RDS instance live in the MouseScoring
# Terraform state. This project only ever READS them through data sources.
# Never declare them as resources here, and never modify them from here:
# two states owning one object is how drift and "who deleted my rule" happen.
#
# Safe to add from this project: standalone rules/attachments that reference
# these IDs (e.g. aws_vpc_security_group_ingress_rule), as long as the target
# group does not declare inline ingress/egress blocks in MouseScoring.
# See docs/shared_infrastructure.md.
#############################################

data "aws_vpc" "shared" {
  tags = { Name = "mousescoring-vpc" }
}

data "aws_subnets" "shared_private" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.shared.id]
  }
  tags = { Name = "mousescoring-private-*" }
}

data "aws_security_group" "shared_rds" {
  vpc_id = data.aws_vpc.shared.id
  name   = "mousescoring-rds-sg"
}

data "aws_security_group" "shared_endpoints" {
  vpc_id = data.aws_vpc.shared.id
  name   = "mousescoring-endpoints-sg"
}

data "aws_db_instance" "shared_postgres" {
  db_instance_identifier = "mousescoring-postgres"
}
