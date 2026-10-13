terraform {
  # Same state bucket and lock table as MouseScoring, but its own key, so the
  # two projects never share (or lock) a state file.
  backend "s3" {
    bucket         = "mousescoring-terraform-state"
    key            = "hydra24/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "mousescoring-terraform-locks"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  required_version = ">= 1.7.0" # for_each in import blocks (imports.tf)
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Product = "Hydra24"
    }
  }
}

# CloudFront certificates must live in us-east-1
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"

  default_tags {
    tags = {
      Product = "Hydra24"
    }
  }
}
