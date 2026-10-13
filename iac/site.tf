#############################################
# hydra24.co.za landing page
# S3 (private) + CloudFront (OAC), same pattern as MouseScoring's active_site.tf.
# The Route53 zone was created outside Terraform, so it is looked up, not owned.
#############################################

locals {
  hydra24_domain = "hydra24.co.za"
}

data "aws_route53_zone" "hydra24" {
  name         = local.hydra24_domain
  private_zone = false
}

#############################################
# S3 bucket (private)
#############################################
resource "aws_s3_bucket" "hydra24_site" {
  bucket_prefix = "hydra24-site-"
  force_destroy = true
}

resource "aws_s3_bucket_public_access_block" "hydra24_site" {
  bucket                  = aws_s3_bucket.hydra24_site.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_ownership_controls" "hydra24_site" {
  bucket = aws_s3_bucket.hydra24_site.id
  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

#############################################
# ACM certificate (CloudFront requires us-east-1)
#############################################
resource "aws_acm_certificate" "hydra24" {
  provider                  = aws.us_east_1
  domain_name               = local.hydra24_domain
  subject_alternative_names = ["www.${local.hydra24_domain}"]
  validation_method         = "DNS"
  tags                      = { Name = "Hydra24 CloudFront Certificate" }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "hydra24_cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.hydra24.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = data.aws_route53_zone.hydra24.zone_id
}

resource "aws_acm_certificate_validation" "hydra24" {
  provider                = aws.us_east_1
  certificate_arn         = aws_acm_certificate.hydra24.arn
  validation_record_fqdns = [for record in aws_route53_record.hydra24_cert_validation : record.fqdn]
}

#############################################
# CloudFront with Origin Access Control
#############################################
resource "aws_cloudfront_origin_access_control" "hydra24_oac" {
  name                              = "hydra24-site-oac"
  description                       = "OAC for hydra24.co.za S3 website"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# Static page with no scripts, so the CSP can be much tighter than the main site's.
resource "aws_cloudfront_response_headers_policy" "hydra24_security_headers" {
  name    = "hydra24-security-headers"
  comment = "Security headers for hydra24.co.za"

  security_headers_config {
    content_type_options {
      override = true
    }

    frame_options {
      frame_option = "DENY"
      override     = true
    }

    referrer_policy {
      referrer_policy = "strict-origin-when-cross-origin"
      override        = true
    }

    strict_transport_security {
      access_control_max_age_sec = 31536000
      include_subdomains         = true
      override                   = true
    }

    content_security_policy {
      content_security_policy = "default-src 'self'; img-src 'self'; style-src 'self'; script-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'none'"
      override                = true
    }
  }
}

resource "aws_cloudfront_distribution" "hydra24_site" {
  enabled             = true
  comment             = "hydra24.co.za landing page"
  default_root_object = "index.html"
  aliases             = [local.hydra24_domain, "www.${local.hydra24_domain}"]
  price_class         = var.site_price_class

  origin {
    domain_name              = aws_s3_bucket.hydra24_site.bucket_regional_domain_name
    origin_id                = "s3-hydra24-site"
    origin_access_control_id = aws_cloudfront_origin_access_control.hydra24_oac.id
  }

  default_cache_behavior {
    target_origin_id           = "s3-hydra24-site"
    viewer_protocol_policy     = "redirect-to-https"
    allowed_methods            = ["GET", "HEAD", "OPTIONS"]
    cached_methods             = ["GET", "HEAD", "OPTIONS"]
    compress                   = true
    response_headers_policy_id = aws_cloudfront_response_headers_policy.hydra24_security_headers.id

    min_ttl     = 0
    default_ttl = 300  # 5 minutes
    max_ttl     = 3600 # 1 hour

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }
  }

  # Single-page site: any miss shows the landing page, with a real 404 status.
  custom_error_response {
    error_code            = 403
    response_code         = 404
    response_page_path    = "/index.html"
    error_caching_min_ttl = 0
  }

  custom_error_response {
    error_code            = 404
    response_code         = 404
    response_page_path    = "/index.html"
    error_caching_min_ttl = 0
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    # Validated ARN so CloudFront is not created before the cert is issued
    acm_certificate_arn      = aws_acm_certificate_validation.hydra24.certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }
}

#############################################
# Bucket policy to grant CloudFront access via OAC
#############################################
resource "aws_s3_bucket_policy" "hydra24_site" {
  bucket = aws_s3_bucket.hydra24_site.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowCloudFrontOACRead"
        Effect    = "Allow"
        Principal = { Service = "cloudfront.amazonaws.com" }
        Action    = ["s3:GetObject"]
        Resource  = ["${aws_s3_bucket.hydra24_site.arn}/*"]
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.hydra24_site.arn
          }
        }
      }
    ]
  })

  depends_on = [
    aws_s3_bucket_public_access_block.hydra24_site,
    aws_s3_bucket_ownership_controls.hydra24_site
  ]
}

#############################################
# DNS: apex + www (A and AAAA) -> CloudFront
#############################################
resource "aws_route53_record" "hydra24_alias" {
  for_each = {
    for pair in setproduct([local.hydra24_domain, "www.${local.hydra24_domain}"], ["A", "AAAA"]) :
    "${pair[0]}-${pair[1]}" => { name = pair[0], type = pair[1] }
  }

  zone_id = data.aws_route53_zone.hydra24.zone_id
  name    = each.value.name
  type    = each.value.type

  alias {
    name                   = aws_cloudfront_distribution.hydra24_site.domain_name
    zone_id                = aws_cloudfront_distribution.hydra24_site.hosted_zone_id
    evaluate_target_health = false
  }
}

#############################################
# Outputs (used by deploy.sh)
#############################################
output "hydra24_site_bucket" {
  description = "S3 bucket name for hydra24.co.za"
  value       = aws_s3_bucket.hydra24_site.bucket
}

output "hydra24_site_distribution_id" {
  description = "CloudFront Distribution ID for hydra24.co.za"
  value       = aws_cloudfront_distribution.hydra24_site.id
}

output "hydra24_site_cloudfront_domain" {
  description = "CloudFront domain for hydra24.co.za"
  value       = aws_cloudfront_distribution.hydra24_site.domain_name
}
