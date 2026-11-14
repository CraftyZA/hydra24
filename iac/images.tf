#############################################
# Event images
# Private bucket, served by the site's CloudFront at /event-images/* (so the
# pages load them from 'self'). Admins' browsers upload to it directly with a
# presigned PUT from the backend; the backend deletes replaced images through
# MouseScoring's S3 gateway endpoint. Same idea as MouseScoring's images.tf.
#############################################

resource "aws_s3_bucket" "images" {
  bucket_prefix = "hydra24-images-"
}

resource "aws_s3_bucket_public_access_block" "images" {
  bucket                  = aws_s3_bucket.images.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_ownership_controls" "images" {
  bucket = aws_s3_bucket.images.id
  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

# Presigned uploads come straight from the site's pages.
resource "aws_s3_bucket_cors_configuration" "images" {
  bucket = aws_s3_bucket.images.id

  cors_rule {
    allowed_methods = ["PUT"]
    allowed_origins = local.hydra24_origins
    allowed_headers = ["content-type"]
    max_age_seconds = 3600
  }
}

resource "aws_s3_bucket_policy" "images" {
  bucket = aws_s3_bucket.images.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowCloudFrontOACRead"
        Effect    = "Allow"
        Principal = { Service = "cloudfront.amazonaws.com" }
        Action    = ["s3:GetObject"]
        Resource  = ["${aws_s3_bucket.images.arn}/event-images/*"]
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.hydra24_site.arn
          }
        }
      }
    ]
  })

  depends_on = [
    aws_s3_bucket_public_access_block.images,
    aws_s3_bucket_ownership_controls.images
  ]
}

# The backend signs uploads with its own role, so the role needs PutObject.
resource "aws_iam_role_policy" "backend_images" {
  name = "event-images"
  role = aws_iam_role.backend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:PutObject", "s3:DeleteObject"]
      Resource = "${aws_s3_bucket.images.arn}/event-images/*"
    }]
  })
}

# S3 through MouseScoring's gateway endpoint: a rule on Hydra24's own group only.
data "aws_ec2_managed_prefix_list" "s3" {
  name = "com.amazonaws.${var.region}.s3"
}

resource "aws_vpc_security_group_egress_rule" "backend_to_s3" {
  security_group_id = aws_security_group.backend.id
  prefix_list_id    = data.aws_ec2_managed_prefix_list.s3.id
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  description       = "S3 (event images) via the gateway endpoint"
}

locals {
  # Where presigned URLs point: the SDK may use either form for us-east-1.
  images_upload_origins = "https://${aws_s3_bucket.images.bucket}.s3.amazonaws.com https://${aws_s3_bucket.images.bucket_regional_domain_name}"
}
