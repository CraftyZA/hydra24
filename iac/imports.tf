#############################################
# One-off adoption of the live hydra24.co.za site from the MouseScoring state.
#
# These resources were first applied from mousescoring/iac/hydra24.tf. The
# import blocks hand them to this state without recreating anything; the
# matching `removed` blocks are in mousescoring/iac/hydra24_removed.tf.
# See docs/state_migration.md. Delete this file once the import is applied.
#
# aws_acm_certificate_validation is not importable; it is a wait-only resource,
# so it is simply "created" again with no AWS change.
#############################################

import {
  to = aws_s3_bucket.hydra24_site
  id = "hydra24-site-20261007123317115800000001"
}

import {
  to = aws_s3_bucket_public_access_block.hydra24_site
  id = "hydra24-site-20261007123317115800000001"
}

import {
  to = aws_s3_bucket_ownership_controls.hydra24_site
  id = "hydra24-site-20261007123317115800000001"
}

import {
  to = aws_s3_bucket_policy.hydra24_site
  id = "hydra24-site-20261007123317115800000001"
}

import {
  provider = aws.us_east_1
  to       = aws_acm_certificate.hydra24
  id       = "arn:aws:acm:us-east-1:837171932043:certificate/2fc121a1-1bb0-4b6f-9623-76a4811d4bcb"
}

import {
  to = aws_route53_record.hydra24_cert_validation["hydra24.co.za"]
  id = "Z02836313SWO2TBXYFGO3__90efd0cdf265c7fc4aa237f22de5fc0b.hydra24.co.za._CNAME"
}

import {
  to = aws_route53_record.hydra24_cert_validation["www.hydra24.co.za"]
  id = "Z02836313SWO2TBXYFGO3__2dc6312c942e220874f71e101870d096.www.hydra24.co.za._CNAME"
}

import {
  to = aws_cloudfront_origin_access_control.hydra24_oac
  id = "EZT4MEWPDLLP1"
}

import {
  to = aws_cloudfront_response_headers_policy.hydra24_security_headers
  id = "b7b07baf-031e-48af-8691-dc992f8d3443"
}

import {
  to = aws_cloudfront_distribution.hydra24_site
  id = "E1VCT64BEV9A4Q"
}

import {
  for_each = toset(["hydra24.co.za-A", "hydra24.co.za-AAAA", "www.hydra24.co.za-A", "www.hydra24.co.za-AAAA"])
  to       = aws_route53_record.hydra24_alias[each.key]
  id       = "Z02836313SWO2TBXYFGO3_${split("-", each.key)[0]}_${split("-", each.key)[1]}"
}
