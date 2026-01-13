
# find-my-nino-add-to-wallet

## About
This repository is the backend for the Save your National Insurance number frontend service (find-my-nino-add-to-wallet-frontend).
It provides the endpoints to create and retrieve the generated digital wallet card.

This repository also fetches individual details from NPS and integrates with NPS for the purpose of upgrading a CRN (Child Reference Number) to a NINO.

## Endpoints
There are a total of 8 endpoints in use in the backend microservice:

- GET         /get-pass-card?passId=${passId}
- GET         /get-qr-code?passId=${passId}
- POST        /create-apple-pass
- GET         /get-google-pass-url?passId=${passId}
- GET         /get-google-qr-code?passId=${passId}
- POST        /create-google-pass-with-credentials

- GET         /individuals/details/NINO/:nino/:resolveMerge   
  PUT         /adult-registration/:identifier                 

Local development (Apple and Google Wallet)

For local development, Apple and Google Wallet credentials must not be committed to the repository.

A helper script is provided to set up the required local environment variables:

scripts/setup-local-wallet-env.sh

The script:

Creates a local secrets directory outside the repository

Generates Apple Wallet development certificates automatically (self-signed)

Base64-encodes the generated certificates

Generates a local environment file used by the service

Validates the presence of Google Wallet credentials

The script is idempotent and can be run multiple times safely.

Google Wallet (local setup)

Google Wallet requires a service account JSON key from a Google Cloud dev or sandbox project.

This file must be downloaded manually from the Google Cloud Console and saved locally as:

~/.hmrc/find-my-nino-add-to-wallet/google-wallet-dev.json

This file must not be committed to git.

Running locally

Before starting the service locally, run:

scripts/setup-local-wallet-env.sh
source target/wallet-local/wallet-local.env

Then start the service as usual (for example, using sbt).

Apple Wallet signing

Apple Wallet signing is enforced by default.

For local development, signing is disabled via configuration to allow passes to be generated using self-signed development certificates.

Signing remains enforced in all other environments.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
