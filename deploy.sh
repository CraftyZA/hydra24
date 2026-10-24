#!/bin/bash
set -euo pipefail

# Deploys the hydra24.co.za landing page: applies this project's Terraform (iac/),
# then uploads ui/ to S3 and invalidates CloudFront.
cd "$(dirname "$0")"

IAC_DIR="iac"
UI_DIR="ui"

# Step 1: Terraform. This project has its own state, so a full apply only ever
# touches hydra24 resources.
echo "Step 1/2: Applying hydra24 infrastructure..."
terraform -chdir="$IAC_DIR" init -input=false > /dev/null
terraform -chdir="$IAC_DIR" apply -input=false

S3_BUCKET=$(terraform -chdir="$IAC_DIR" output -raw hydra24_site_bucket)
CLOUDFRONT_DIST_ID=$(terraform -chdir="$IAC_DIR" output -raw hydra24_site_distribution_id)

# Step 2: Upload. HTML/CSS/JS revalidate every load; images get a day.
echo ""
echo "Step 2/2: Uploading $UI_DIR to s3://$S3_BUCKET/ ..."
aws s3 sync "$UI_DIR/" "s3://$S3_BUCKET/" \
    --exclude="*" --include="*.html" --include="*.css" --include="*.js" \
    --cache-control "no-cache" --delete --only-show-errors
aws s3 sync "$UI_DIR/" "s3://$S3_BUCKET/" \
    --exclude="*" --include="*.jpg" --include="*.jpeg" --include="*.png" --include="*.ico" \
    --cache-control "public, max-age=86400" --delete --only-show-errors

INVALIDATION_ID=$(aws cloudfront create-invalidation --distribution-id "$CLOUDFRONT_DIST_ID" \
    --paths "/*" --query 'Invalidation.Id' --output text)
echo "CloudFront invalidation: $INVALIDATION_ID"

echo ""
for path in / /styles.css /images/construction.png /images/hydra24.jpg; do
    echo "  https://hydra24.co.za$path -> $(curl -s -o /dev/null -w '%{http_code} %{content_type}' "https://hydra24.co.za$path")"
done
echo "Done: https://hydra24.co.za"
