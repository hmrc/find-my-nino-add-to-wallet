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
import helper.ApiPayloadHelper
import play.api.libs.json.*

class IndividualDetailsSpec extends SpecBase with ApiPayloadHelper {
  import transformations.IndividualDetails.*

  "reads" must {
    "transform correctly - latest name type 'known as' (Mr & no honours) and address type 'correspondance' & crn ind 1" in {
      val result: JsResult[JsObject] =
        apiIndividualDetailsJsonThreeNamesThreeAddresses.validate[JsObject](IndividualDetails.reads)
      result mustBe JsSuccess(Json.parse(s"""{
        |   "title":"Mr",
        |   "firstForename":"name31",
        |   "secondForename":"name32",
        |   "surname":"surname3",
        |   "dateOfBirth":"1990-07-20",
        |   "nino":"$generatedNino",
        |   "address":{
        |      "addressLine1":"addr31",
        |      "addressCountry":"GREAT BRITAIN",
        |      "addressLine3":"addr33",
        |      "addressLine2":"addr32",
        |      "addressType":$AddressTypeCorrespondance,
        |      "addressLine5":"addr35",
        |      "addressStartDate":"2018-03-10",
        |      "addressPostcode":"postcode3",
        |      "addressLine4":"addr34"
        |   },
        |   "crnIndicator":"true"
        |}""".stripMargin))
    }

    "transform correctly - only name type 'known as' (Mr & no honours) and address type 'correspondance' & crn ind 1" in {
      val result: JsResult[JsObject] =
        apiIndividualDetailsJsonTwoNamesTwoAddresses.validate[JsObject](IndividualDetails.reads)
      result mustBe JsSuccess(Json.parse(s"""{
        |   "title":"Mr",
        |   "firstForename":"name21",
        |   "secondForename":"name22",
        |   "surname":"surname2",
        |   "dateOfBirth":"1990-07-20",
        |   "nino":"$generatedNino",
        |   "address":{
        |      "addressLine1":"addr21",
        |      "addressCountry":"GREAT BRITAIN",
        |      "addressLine3":"addr23",
        |      "addressLine2":"addr22",
        |      "addressType":$AddressTypeCorrespondance,
        |      "addressLine5":"addr25",
        |      "addressStartDate":"2018-03-10",
        |      "addressPostcode":"postcode2",
        |      "addressLine4":"addr24"
        |   },
        |   "crnIndicator":"true"
        |}""".stripMargin))
    }
    "transform correctly - only 1 name of type 'real' (Dr with honours) and 1 address of type 'residential' + missing opt address lines & crn ind 0" in {
      val result: JsResult[JsObject] =
        apiIndividualDetailsJsonOneNameOneAddress.validate[JsObject](IndividualDetails.reads)
      result mustBe JsSuccess(apiTransformedIndividualDetailsJsonOneNameOneAddress)
    }
  }
}
