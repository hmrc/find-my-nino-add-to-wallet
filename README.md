
# find-my-nino-add-to-wallet

## About
This repository is the backend for the Store my NINO frontend service (find-my-nino-add-to-wallet-frontend).
It provides the endpoints to retrieve person details, and create and retrieve the generated digital wallet card.

## Endpoints
There are total of 7 endpoints in the backend microservice, details for each are below the list of endpoints:

- GET         /get-pass-details?passId=${passId}
- GET         /get-pass-details-by-name-and-nino?fullName=${fullName}&nino=${nino}
- GET         /get-pass-card?passId=${passId}
- GET         /get-qr-code?passId=${passId}
- GET         /get-person-details?pdId=${personDetailsId}
- POST        /create-person-details
- POST        /create-apple-pass

## How to test
TODO

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").