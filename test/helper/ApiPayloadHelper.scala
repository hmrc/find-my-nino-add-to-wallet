/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helper

import play.api.libs.json.{JsArray, JsObject, Json}
import transformations.IndividualDetails.{AddressTypeCorrespondance, AddressTypeResidential, NameTypeKnownAs, NameTypeReal}
import uk.gov.hmrc.domain.Generator

trait ApiPayloadHelper {

  protected def individualDetailsApiFull(
    mainSection: JsObject,
    names: Seq[JsObject],
    addresses: Seq[JsObject]
  ): JsObject =
    mainSection ++ Json.obj(
      "nameList" -> Json.obj(
        "name" -> JsArray(names)
      )
    ) ++ Json.obj(
      "addressList" -> Json.obj(
        "address" -> JsArray(addresses)
      )
    )

  protected def individualDetailsApiNameSection(
    seqNo: Int,
    nameType: Int,
    titleType: Int,
    name1: String,
    name2: Option[String] = None,
    surname: String,
    honours: Option[String] = None
  ): JsObject = Json.obj(
    "nameSequenceNumber" -> seqNo,
    "nameType"           -> nameType,
    "titleType"          -> titleType,
    "firstForename"      -> name1,
    "surname"            -> surname
  )
    ++ honours.fold(Json.obj())(h => Json.obj("honours" -> h))
    ++ name2.fold(Json.obj())(h => Json.obj("secondForename" -> h))

  protected def individualDetailsApiAddressSection(
    seqNo: Int,
    addressType: Int,
    addressStatus: Int,
    addr1: String,
    addr2: String,
    addr3: Option[String] = None,
    addr4: Option[String] = None,
    addr5: Option[String] = None,
    postcode: Option[String] = None
  ): JsObject = Json.obj(
    "addressSequenceNumber" -> seqNo,
    "countryCode"           -> 1,
    "addressType"           -> addressType,
    "addressStatus"         -> addressStatus,
    "addressStartDate"      -> "2018-03-10",
    "addressLine1"          -> addr1,
    "addressLine2"          -> addr2
  ) ++ addr3.fold(Json.obj())(a => Json.obj("addressLine3" -> a))
    ++ addr4.fold(Json.obj())(a => Json.obj("addressLine4" -> a))
    ++ addr5.fold(Json.obj())(a => Json.obj("addressLine5" -> a))
    ++ postcode.fold(Json.obj())(a => Json.obj("addressPostcode" -> a))

  protected val (generatedNinoWithoutSuffix, generatedNinoSuffix) = {
    val generatedNino: String = new Generator().nextNino.nino
    (generatedNino.take(8), generatedNino.takeRight(1))
  }

  protected def individualDetailsApiResponseMain(
    crnIndicator: Int,
    ninoWithoutSuffix: String,
    ninoSuffix: String
  ): JsObject = Json
    .parse(s"""{
              |  "details": {
              |    "nino": "$ninoWithoutSuffix",
              |    "ninoSuffix": "$ninoSuffix",
              |    "dateOfBirth": "1990-07-20",
              |    "dateOfBirthStatus": 2,
              |    "dateOfDeath": "2015-06-15",
              |    "dateOfDeathStatus": 0,
              |    "dateOfRegistration": "2005-03-25",
              |    "registrationType": 6,
              |    "adultRegSerialNumber": "23456ARS",
              |    "cesaAgentIdentifier": "ZXCVBN/1",
              |    "cesaAgentClientReference": "JOHN DOE",
              |    "permanentTSuffixCaseIndicator": 2,
              |    "currOptimisticLock": 65,
              |    "liveCapacitorInd": 0,
              |    "liveAgentInd": 1,
              |    "ntTaxCodeInd": 1,
              |    "mergeStatus": 0,
              |    "marriageStatusType": 3,
              |    "crnIndicator": $crnIndicator
              |  },
              |  "indicators": {
              |    "manualCodingInd": 2,
              |    "manualCodingReason": 7,
              |    "manualCodingOther": "INCOMPLETE DATA",
              |    "manualCorrInd": 2,
              |    "manualCorrReason": "INFORMATION UPDATE",
              |    "additionalNotes": "UPDATED PHONE NUMBER",
              |    "deceasedInd": 0,
              |    "s128Ind": 1,
              |    "noAllowInd": 0,
              |    "eeaCmnwthInd": 1,
              |    "noRepaymentInd": 1,
              |    "saLinkInd": 0,
              |    "noATSInd": 0,
              |    "taxEqualBenInd": 0,
              |    "p2ToAgentInd": 1,
              |    "digitallyExcludedInd": 0,
              |    "bankruptcyInd": 0,
              |    "bankruptcyFiledDate": "2010-09-18",
              |    "utr": "7654/32ABC",
              |    "audioOutputInd": 1,
              |    "welshOutputInd": 0,
              |    "largePrintOutputInd": 0,
              |    "brailleOutputInd": 0,
              |    "specialistBusinessArea": 8,
              |    "saStartYear": "2010/11",
              |    "saFinalYear": "2022",
              |    "digitalP2Ind": 1
              |  },
              |  "residencyList": {
              |    "residency": [
              |      {
              |        "residencySequenceNumber": 98765,
              |        "dateLeavingUK": "2020-04-15",
              |        "dateReturningUK": "2024-09-22",
              |        "residencyStatusFlag": 2
              |      }
              |    ]
              |  }
              |}
              |""".stripMargin)
    .as[JsObject]

  protected lazy val apiIndividualDetailsJsonThreeNamesThreeAddresses: JsObject = individualDetailsApiFull(
    individualDetailsApiResponseMain(crnIndicator = 1, generatedNinoWithoutSuffix, generatedNinoSuffix),
    Seq(
      individualDetailsApiNameSection(
        seqNo = 1,
        nameType = NameTypeReal,
        titleType = 5,
        name1 = "name11",
        name2 = Some("name12"),
        surname = "surname1"
      ),
      individualDetailsApiNameSection(
        seqNo = 2,
        nameType = NameTypeKnownAs,
        titleType = 1,
        name1 = "name21",
        name2 = Some("name22"),
        surname = "surname2"
      ),
      individualDetailsApiNameSection(
        seqNo = 3,
        nameType = NameTypeKnownAs,
        titleType = 1,
        name1 = "name31",
        name2 = Some("name32"),
        surname = "surname3"
      )
    ),
    Seq(
      individualDetailsApiAddressSection(
        seqNo = 1,
        addressType = AddressTypeResidential,
        addressStatus = 6,
        addr1 = "addr11",
        addr2 = "addr12",
        addr3 = Some("addr13"),
        addr4 = Some("addr14"),
        addr5 = Some("addr15"),
        postcode = Some("postcode1")
      ),
      individualDetailsApiAddressSection(
        seqNo = 2,
        addressType = AddressTypeCorrespondance,
        addressStatus = 6,
        addr1 = "addr21",
        addr2 = "addr22",
        addr3 = Some("addr23"),
        addr4 = Some("addr24"),
        addr5 = Some("addr25"),
        postcode = Some("postcode2")
      ),
      individualDetailsApiAddressSection(
        seqNo = 3,
        addressType = AddressTypeCorrespondance,
        addressStatus = 6,
        addr1 = "addr31",
        addr2 = "addr32",
        addr3 = Some("addr33"),
        addr4 = Some("addr34"),
        addr5 = Some("addr35"),
        postcode = Some("postcode3")
      )
    )
  )

  protected lazy val apiIndividualDetailsJsonTwoNamesTwoAddresses: JsObject = individualDetailsApiFull(
    individualDetailsApiResponseMain(crnIndicator = 1, generatedNinoWithoutSuffix, generatedNinoSuffix),
    Seq(
      individualDetailsApiNameSection(
        seqNo = 1,
        nameType = NameTypeReal,
        titleType = 5,
        name1 = "name11",
        name2 = Some("name12"),
        surname = "surname1"
      ),
      individualDetailsApiNameSection(
        seqNo = 2,
        nameType = NameTypeKnownAs,
        titleType = 1,
        name1 = "name21",
        surname = "surname2"
      )
    ),
    Seq(
      individualDetailsApiAddressSection(
        seqNo = 1,
        addressType = AddressTypeResidential,
        addressStatus = 6,
        addr1 = "addr11",
        addr2 = "addr12",
        addr3 = Some("addr13"),
        addr4 = Some("addr14"),
        addr5 = Some("addr15"),
        postcode = Some("postcode1")
      ),
      individualDetailsApiAddressSection(
        seqNo = 2,
        addressType = AddressTypeCorrespondance,
        addressStatus = 6,
        addr1 = "addr21",
        addr2 = "addr22",
        addr3 = Some("addr23"),
        addr4 = Some("addr24"),
        addr5 = Some("addr25"),
        postcode = Some("postcode2")
      )
    )
  )

  protected lazy val apiIndividualDetailsJsonOneNameOneAddress: JsObject = individualDetailsApiFull(
    individualDetailsApiResponseMain(crnIndicator = 0, generatedNinoWithoutSuffix, generatedNinoSuffix),
    Seq(
      individualDetailsApiNameSection(
        seqNo = 1,
        nameType = NameTypeReal,
        titleType = 5,
        name1 = "name11",
        name2 = Some("name12"),
        surname = "surname1",
        honours = Some("BA")
      )
    ),
    Seq(
      individualDetailsApiAddressSection(
        seqNo = 1,
        addressType = AddressTypeResidential,
        addressStatus = 6,
        addr1 = "addr11",
        addr2 = "addr12"
      )
    )
  )

  protected def apiTransformedIndividualDetailsJsonOneNameOneAddress: JsObject = Json
    .parse(s"""{
                |   "title":"Dr",
                |   "firstForename":"name11",
                |   "secondForename":"name12",
                |   "surname":"surname1",
                |   "honours":"BA",
                |   "dateOfBirth":"1990-07-20",
                |   "nino":"$generatedNinoWithoutSuffix$generatedNinoSuffix",
                |   "address":{
                |      "addressLine1":"addr11",
                |      "addressCountry":"GREAT BRITAIN",
                |      "addressLine2":"addr12",
                |      "addressType":$AddressTypeResidential,
                |      "addressStartDate":"2018-03-10"
                |   },
                |   "crnIndicator":"false"
                |}""".stripMargin)
    .as[JsObject]

}
