#############################################
# Signing keys for backend-side ID token verification
#
# The backend verifies every bearer token itself (JwtVerifier); API Gateway
# has no authorizer. It runs in private subnets with no NAT, so it cannot
# fetch Cognito's JWKS: Terraform fetches the pool's public keys here and
# passes them to the Lambda. Cognito does not rotate a pool's keys; a new
# pool gets new keys on the same apply that creates it.
#############################################

data "http" "cognito_jwks" {
  url             = "${local.cognito_issuer}/.well-known/jwks.json"
  request_headers = { Accept = "application/json" }

  lifecycle {
    postcondition {
      condition     = self.status_code == 200 && length(jsondecode(self.response_body).keys) > 0
      error_message = "Could not fetch the Cognito JWKS; the backend would refuse every sign-in."
    }
  }
}

locals {
  cognito_signing_keys = jsonencode({
    for key in jsondecode(data.http.cognito_jwks.response_body).keys :
    key.kid => { n = key.n, e = key.e } if key.kty == "RSA"
  })
}
