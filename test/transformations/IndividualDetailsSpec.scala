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

package transformations

import _root_.util.SpecBase
import play.api.libs.json.*
import uk.gov.hmrc.domain._

class IndividualDetailsSpec extends SpecBase {
  import IndividualDetailsSpec.*

  "reads" must {
    "transform using name with name type 2 ('known as' name) and known title type" in {
      val jsonToTransform: JsObject = validApiResponseMinusNamesAndAddressesJson ++ Json.obj(
        "nameList" -> Json.obj(
          "name" -> JsArray(
            Seq(
              name(1, 3, 5, "name11", "name12", "surname1", "2010-02-05", "2022-11-15"),
              name(2, 2, 1, "name21", "name22", "surname2", "2010-02-05", "2022-11-15")
            )
          )
        )
      ) ++ Json.obj(
        "addressList" -> Json.obj(
          "address" -> JsArray(
            Seq(
              address(1, 3, 5, 6, "addr11", "addr12", Some("addr13"), Some("addr14"), Some("addr15"), Some("postcode1")),
              address(1, 2, 2, 6, "addr21", "addr22", Some("addr23"), Some("addr24"), Some("addr25"), Some("postcode2"))
            )
          )
        )
      )




      val expTransformedJson = Json.parse(s"""{
         |   "title":"Mr",
         |   "firstForename":"name21",
         |   "secondForename":"name22",
         |   "surname":"surname2",
         |   "honours":"BA",
         |   "dateOfBirth":"1990-07-20",
         |   "nino":"$generatedNino",
         |   "address":{
         |      "addressLine1":"addr21",
         |      "addressCountry":"GREAT BRITAIN",
         |      "addressLine3":"addr23",
         |      "addressLine2":"addr22",
         |      "addressType":2,
         |      "addressLine5":"addr25",
         |      "addressStartDate":"2018-03-10",
         |      "addressPostcode":"postcode2",
         |      "addressLine4":"addr24"
         |   },
         |   "crnIndicator":"true"
         |}""".stripMargin)

      val result: JsResult[JsObject] = jsonToTransform.validate[JsObject](IndividualDetails.reads)
      result mustBe JsSuccess(expTransformedJson)
      
      /*

       */
      
    }
  }

}

object IndividualDetailsSpec {

  private def name(
    seqNo: Int,
    nameType: Int,
    titleType: Int,
    name1: String,
    name2: String,
    surname: String,
    startDate: String,
    endDate: String
  ) = Json.obj(
    "nameSequenceNumber" -> seqNo,
    "nameType"           -> nameType,
    "titleType"          -> titleType,
    "requestedName"      -> s"$name1 $name2 $surname",
    "nameStartDate"      -> startDate,
    "nameEndDate"        -> endDate,
    "otherTitle"         -> "other title",
    "honours"            -> "BA",
    "firstForename"      -> name1,
    "secondForename"     -> name2,
    "surname"            -> surname
  )
  
  private def address(
                       seqNo: Int,
                       addressSource: Int,
                       addressType: Int,
                       addressStatus: Int,
                       addr1: String,
                       addr2: String,
                       addr3: Option[String],
                       addr4: Option[String],
                       addr5: Option[String],
                       postcode: Option[String]
  ) = Json.obj(
    "addressSequenceNumber"-> seqNo,
    "addressSource"-> addressSource,
    "countryCode"-> 1, // GREAT BRITAIN
    "addressType"-> addressType,
    "addressStatus"-> addressStatus,
    "addressStartDate"-> "2018-03-10",
    "addressEndDate"-> "2025-12-31",
    "addressLastConfirmedDate"-> "2022-09-30",
    "vpaMail"-> 101,
    "deliveryInfo"-> "ELECTRONIC DELIVERY",
    "pafReference"-> "PAF67890",
    "addressLine1"-> addr1,
    "addressLine2"-> addr2
  ) ++ addr3.fold(Json.obj())(a => Json.obj("addressLine3" -> a))
    ++ addr4.fold(Json.obj())(a => Json.obj("addressLine4" -> a))
    ++ addr5.fold(Json.obj())(a => Json.obj("addressLine5" -> a))
    ++ postcode.fold(Json.obj())(a => Json.obj("addressPostcode" -> a))




  private val generatedNino: String = new Generator().nextNino.nino

  private val validApiResponseMinusNamesAndAddressesJson: JsObject = Json
    .parse(s"""{
                                                           |  "details": {
                                                           |    "nino": "$generatedNino",
                                                           |    "ninoSuffix": "C",
                                                           |    "accountStatusType": 2,
                                                           |    "sex": "U",
                                                           |    "dateOfEntry": "2004-11-07",
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
                                                           |    "crnIndicator": 1
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
}
