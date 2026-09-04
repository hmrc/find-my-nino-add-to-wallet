# find-my-nino-add-to-wallet
=================================

Allows users to create and retrieve digital wallet passes for their National Insurance number (NINO), and supports individual details retrieval and CRN-to-NINO uplift integration with NPS.

Requirements
------------

This service is written in [Scala 3.x](http://www.scala-lang.org/) and [Play 3.x](http://playframework.com/), so needs at least a [JRE 21](http://www.oracle.com/technetwork/java/javase/downloads/index.html) to run.

API
---

| *Task* | *Supported Methods* | *Description* | Status |
|--------|----------------------|---------------|--------|
| `/find-my-nino-add-to-wallet/create-apple-pass` | POST | Creates an Apple Wallet pass payload for a user. | Live |
| `/find-my-nino-add-to-wallet/get-pass-card?passId=:passId` | GET | Retrieves an Apple Wallet `.pkpass` card by pass ID. | Live |
| `/find-my-nino-add-to-wallet/get-qr-code?passId=:passId` | GET | Retrieves an Apple Wallet QR code by pass ID. | Live |
| `/find-my-nino-add-to-wallet/get-google-pass-url?passId=:passId` | GET | Retrieves a Google Wallet save URL for a pass ID. | Live |
| `/find-my-nino-add-to-wallet/get-google-qr-code?passId=:passId` | GET | Retrieves a Google Wallet QR code by pass ID. | Live |
| `/find-my-nino-add-to-wallet/create-google-pass-with-credentials` | POST | Creates a Google Wallet pass using configured credentials. | Live |
| `/find-my-nino-add-to-wallet/individuals/details/NINO/:nino/:resolveMerge` | GET | Retrieves individual details from NPS-backed services. | Live |
| `/find-my-nino-add-to-wallet/individuals/details/cache/NINO/:nino` | DELETE | Invalidates cached individual details for a NINO. | Live |
| `/find-my-nino-add-to-wallet/adult-registration/:identifier` | PUT | Uplifts an adult CRN identifier to NINO via NPS integration. | Live |
| `/find-my-nino-add-to-wallet/test-only/showCertificatesExpiry` | GET | Returns Apple certificate expiry information for test support. | Test-only |

Configuration
-------------

All downstream services require host and port settings, for example:

| *Key* | *Description* |
|-------|---------------|
| `microservice.services.auth.host` | Host of the Auth service |
| `microservice.services.auth.port` | Port of the Auth service |
| `microservice.services.nps-crn-api.host` | Host of the NPS CRN API service |
| `microservice.services.nps-crn-api.port` | Port of the NPS CRN API service |
| `microservice.services.internal-auth.host` | Host of the Internal Auth service |
| `microservice.services.internal-auth.port` | Port of the Internal Auth service |
| `microservice.services.fandf.host` | Host of the Find-and-Flag service |
| `microservice.services.fandf.port` | Port of the Find-and-Flag service |

For local development:
- Apple pass signing is disabled by default (`applePass.signingEnabled=false`)
- Dummy local values are provided for Apple and Google wallet credential keys
- Real certificates and credentials are required outside local development

How to test the project
=======================

Unit Tests
----------
- **Unit test the entire test suite:** `sbt test`
- **Unit test a single spec file:** `sbt "testOnly *fileName"` (for example: `sbt "testOnly *ApplePassControllerSpec"`)

Integration tests
-----------------
- **Run integration tests:** `sbt it/test`

Acceptance tests
----------------
To verify the acceptance tests locally, follow the steps:
- start the sm2 container for the service profile: `sm2 --start FIND_MY_NINO_ADD_TO_WALLET_ALL`
- stop `FIND_MY_NINO_ADD_TO_WALLET` process running in sm2: `sm2 --stop FIND_MY_NINO_ADD_TO_WALLET`
- launch this service in terminal and execute the following command in the project directory: `sbt "run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"`
- open [find-my-nino-add-to-wallet-acceptance-tests](https://github.com/hmrc/find-my-nino-add-to-wallet-acceptance-tests) repository in the terminal and execute the script: `./run_specs_local.sh`

Acronyms
--------

In the context of this service we use the following acronyms:

* NINO: National Insurance Number
* CRN: Child Reference Number
* NPS: National Insurance and PAYE Service
* API: Application Programming Interface
* JRE: Java Runtime Environment
* JSON: JavaScript Object Notation
* URL: Uniform Resource Locator

License
-------

This code is open source software licensed under the Apache 2.0 License.