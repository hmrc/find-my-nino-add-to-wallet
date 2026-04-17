
# find-my-nino-add-to-wallet

## About
This repository is the backend for the Save your National Insurance number frontend service (`find-my-nino-add-to-wallet-frontend`).
It provides endpoints to create and retrieve generated digital wallet cards.

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

## Local development

For local development, no Apple or Google Wallet certificates need to be downloaded.

The application uses:
- dummy base64 values in configuration for local placeholders
- Apple Wallet signing disabled locally

### Apple Wallet

Apple Wallet signing is disabled for local development:

applePass.signingEnabled = false

Because signing is disabled locally:

no Apple certificates are required
no .p12 files are required
no local certificate generation is required

Signing remains enabled by default in other environments unless explicitly disabled.

### Google Wallet

For local development, the application uses the dummy base64 Google key already present in configuration.

Because of this:

no Google service account key needs to be downloaded for local development
no extra local setup is required for wallet credentials

This local setup is intended for development and flow testing only.

Running locally

Start the service as usual, for example:

sbt run

The local wallet configuration is intended only for development.

Real Apple signing certificates and real Google credentials are still required for non-local environments where genuine wallet signing and integration are needed.

### License

This code is open source software licensed under the Apache 2.0 License.