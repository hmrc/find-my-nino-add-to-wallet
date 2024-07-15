
# find-my-nino-add-to-wallet

## About
This repository is the backend for the Save your National Insurance number frontend service (find-my-nino-add-to-wallet-frontend).
It provides the endpoints to create and retrieve the generated digital wallet card.

This repository also fetches individual details from NPS and also integrates with NPS for the purpose of upgrading a CRN (Child Record Number) to a Nino.

## Endpoints
There are total of 8 endpoints in use in the backend microservice:

- GET         /get-pass-card?passId=${passId}
- GET         /get-qr-code?passId=${passId}
- POST        /create-apple-pass
- GET         /get-google-pass-url?passId=${passId}
- GET         /get-google-qr-code?passId=${passId}
- POST        /create-google-pass-with-credentials

- GET         /individuals/details/NINO/:nino/:resolveMerge   
  PUT         /adult-registration/:identifier                 

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").