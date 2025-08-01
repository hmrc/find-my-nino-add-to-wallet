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
    "transform correctly - latest name type 'known as' (Mr & no honours) and address type 'correspondance'" in {
      val result: JsResult[JsObject] = jsonThreeNamesThreeAddresses.validate[JsObject](IndividualDetails.reads)
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

    "transform correctly - only name type 'known as' (Mr & no honours) and address type 'correspondance'" in {
      val result: JsResult[JsObject] = jsonTwoNamesTwoAddresses.validate[JsObject](IndividualDetails.reads)
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
    "transform correctly - only one name of type 'real' (Dr with honours) and one address of type 'residential' + missing address+postcode lines" in {
      val result: JsResult[JsObject] = jsonOneNameOneAddress.validate[JsObject](IndividualDetails.reads)
      result mustBe JsSuccess(Json.parse(s"""{
        |   "title":"Dr",
        |   "firstForename":"name11",
        |   "secondForename":"name12",
        |   "surname":"surname1",
        |   "honours":"BA",
        |   "dateOfBirth":"1990-07-20",
        |   "nino":"$generatedNino",
        |   "address":{
        |      "addressLine1":"addr11",
        |      "addressCountry":"GREAT BRITAIN",
        |      "addressLine2":"addr12",
        |      "addressType":$AddressTypeResidential,
        |      "addressStartDate":"2018-03-10"
        |   },
        |   "crnIndicator":"true"
        |}""".stripMargin))
    }
  }

  private lazy val jsonThreeNamesThreeAddresses: JsObject = individualDetailsApiFull(
    individualDetailsApiResponseMain,
    Seq(
      individualDetailsApiNameSection(
        seqNo = 1,
        nameType = NameTypeReal,
        titleType = 5,
        name1 = "name11",
        name2 = "name12",
        surname = "surname1"
      ),
      individualDetailsApiNameSection(
        seqNo = 2,
        nameType = NameTypeKnownAs,
        titleType = 1,
        name1 = "name21",
        name2 = "name22",
        surname = "surname2"
      ),
      individualDetailsApiNameSection(
        seqNo = 3,
        nameType = NameTypeKnownAs,
        titleType = 1,
        name1 = "name31",
        name2 = "name32",
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

  private lazy val jsonTwoNamesTwoAddresses: JsObject = individualDetailsApiFull(
    individualDetailsApiResponseMain,
    Seq(
      individualDetailsApiNameSection(
        seqNo = 1,
        nameType = NameTypeReal,
        titleType = 5,
        name1 = "name11",
        name2 = "name12",
        surname = "surname1"
      ),
      individualDetailsApiNameSection(
        seqNo = 2,
        nameType = NameTypeKnownAs,
        titleType = 1,
        name1 = "name21",
        name2 = "name22",
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

  private lazy val jsonOneNameOneAddress: JsObject = individualDetailsApiFull(
    individualDetailsApiResponseMain,
    Seq(
      individualDetailsApiNameSection(
        seqNo = 1,
        nameType = NameTypeReal,
        titleType = 5,
        name1 = "name11",
        name2 = "name12",
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

}
