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
  "reads" must {
    "transform using name with name type 2 ('known as' name) and known title type" in {
      val jsonToTransform: JsObject = individualDetailsApiFull(
        individualDetailsApiResponseMain,
        Seq(
          individualDetailsApiNameSection(
            seqNo = 1,
            nameType = 3,
            titleType = 5,
            name1 = "name11",
            name2 = "name12",
            surname = "surname1",
            startDate = "2010-02-05",
            endDate = "2022-11-15"
          ),
          individualDetailsApiNameSection(
            seqNo = 2,
            nameType = 2,
            titleType = 1,
            name1 = "name21",
            name2 = "name22",
            surname = "surname2",
            startDate = "2010-02-05",
            endDate = "2022-11-15"
          )
        ),
        Seq(
          individualDetailsApiAddressSection(
            seqNo = 1,
            addressSource = 3,
            addressType = 5,
            addressStatus = 6,
            addr1 = "addr11",
            addr2 = "addr12",
            addr3 = Some("addr13"),
            addr4 = Some("addr14"),
            addr5 = Some("addr15"),
            postcode = Some("postcode1")
          ),
          individualDetailsApiAddressSection(
            seqNo = 1,
            addressSource = 2,
            addressType = 2,
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
    }
  }

}
