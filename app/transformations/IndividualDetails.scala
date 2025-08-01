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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*
import play.api.libs.json.Reads.JsObjectReducer

import scala.util.chaining.scalaUtilChainingOps

object IndividualDetails {
  final val NameTypeReal              = 1
  final val NameTypeKnownAs           = 2
  final val AddressTypeResidential    = 1
  final val AddressTypeCorrespondance = 2

  private val doNothing: Reads[JsObject] = __.json.put(Json.obj())

  private def addOptionalField(rds: Reads[JsObject], fieldName: String, optionalValue: Option[String]) =
    rds.map { jsObject =>
      optionalValue match {
        case None    => jsObject
        case Some(v) => jsObject ++ Json.obj(fieldName -> v)
      }
    }

  private val codeToCountry: Map[Int, String] = Map(
    0   -> "NOT SPECIFIED OR NOT USED",
    1   -> "GREAT BRITAIN",
    2   -> "GIBRALTAR",
    3   -> "GUERNSEY",
    4   -> "JERSEY",
    5   -> "MALTA",
    6   -> "SARK",
    7   -> "ISLE OF MAN",
    8   -> "NORTHERN IRELAND",
    9   -> "TOURS",
    10  -> "ALDERNEY",
    11  -> "ANTIGUA",
    12  -> "AUSTRALIA",
    13  -> "BAHAMAS",
    14  -> "BARBADOS",
    15  -> "BERMUDA",
    16  -> "MALDIVE ISLANDS",
    17  -> "CAYMAN ISLANDS",
    18  -> "COOK ISLANDS",
    19  -> "COMMONWEALTH OF DOMINICA",
    20  -> "FALKLAND ISLANDS",
    21  -> "FIJI",
    22  -> "KIRIBATI",
    23  -> "JAMAICA",
    24  -> "HONG KONG",
    25  -> "MAURITIUS",
    26  -> "MONTSERRAT",
    27  -> "VANUATU",
    28  -> "NEW ZEALAND",
    29  -> "NORFOLK ISLAND",
    30  -> "PAPUA NEW GUINEA",
    31  -> "ST HELENA & DEPNDS",
    32  -> "NEVIS,ST KITTS-NEVIS",
    33  -> "ST VINCENT & GRENADINES",
    34  -> "SEYCHELLES",
    35  -> "SOLOMON ISLANDS",
    36  -> "TUVALU",
    37  -> "TRINIDAD & TOBAGO",
    38  -> "TURKS & CAICOS ISLANDS",
    39  -> "VIRGIN ISLANDS (BRITISH)",
    40  -> "WESTERN SAMOA",
    41  -> "ANGUILLA",
    42  -> "GRENADA",
    43  -> "ST LUCIA",
    44  -> "NAURU",
    45  -> "GREENLAND",
    46  -> "CANADA",
    47  -> "GUAM",
    48  -> "ASCENCION ISLAND",
    49  -> "BAHRAIN",
    50  -> "BOTSWANA",
    51  -> "BRUNEI",
    52  -> "BURMA",
    53  -> "SRI LANKA",
    54  -> "BHUTAN",
    55  -> "GAMBIA",
    56  -> "GHANA",
    57  -> "GUYANA",
    58  -> "ANTARCTIC TERRITORIES (BRITISH)",
    59  -> "INDIA",
    60  -> "JORDAN",
    61  -> "KENYA",
    62  -> "KUWAIT",
    63  -> "LESOTHO",
    64  -> "LIBYA",
    65  -> "MALAWI",
    66  -> "MALAYSIA",
    67  -> "OMAN",
    68  -> "NIGERIA",
    69  -> "PAKISTAN",
    70  -> "DEMOCRATIC YEMEN",
    71  -> "QATAR",
    72  -> "SOUTH AFRICA",
    73  -> "SABAH",
    74  -> "SARAWAK",
    75  -> "SHARJAH",
    76  -> "SIERRA LEONE",
    77  -> "SINGAPORE",
    78  -> "SWAZILAND",
    79  -> "TANZANIA",
    80  -> "ARAB EMIRATES (UNITED)",
    81  -> "UGANDA",
    82  -> "ZAMBIA",
    83  -> "NAMIBIA",
    84  -> "BANGLADESH",
    85  -> "TONGA",
    86  -> "BISSAU (GUINEA)",
    87  -> "MONGOLIA",
    88  -> "NORTH KOREA",
    89  -> "PRINCIPE AND SAO TOME",
    90  -> "CAPE VERDE ISLANDS",
    91  -> "COMORO ISLANDS",
    92  -> "EQUATORIAL GUINEA",
    93  -> "AMERICAN SAMOA",
    94  -> "BARBUDA",
    95  -> "VIRGIN ISLANDS (USA)",
    96  -> "ST MARTINS",
    97  -> "RUSSIAN FEDERATION",
    98  -> "REPUBLIC OF ARMENIA",
    99  -> "REPUBLIC OF BELARUS",
    100 -> "REPUBLIC OF KAZAKHSTAN",
    101 -> "REPUBLIC OF KYRGYZSTAN",
    102 -> "REPUBLIC OF MOLDOVA",
    103 -> "REPUBLIC OF TAJIKISTAN",
    104 -> "REPUBLIC OF TURKMENISTAN",
    105 -> "REPUBLIC OF UZBEKISTAN",
    106 -> "REPUBLIC OF AZERBAIJAN",
    107 -> "REPUBLIC OF ESTONIA",
    108 -> "REPUBLIC OF GEORGIA",
    109 -> "REPUBLIC OF LATVIA",
    110 -> "REPUBLIC OF LITHUANIA",
    111 -> "UKRAINE",
    114 -> "ENGLAND",
    115 -> "SCOTLAND",
    116 -> "WALES",
    117 -> "REPUBLIC OF IRELAND",
    120 -> "CHANNEL ISLANDS",
    121 -> "ANDORRA",
    122 -> "AUSTRIA",
    123 -> "BELGIUM",
    124 -> "BULGARIA",
    125 -> "CZECHOSLOVAKIA",
    126 -> "DENMARK",
    127 -> "FINLAND",
    128 -> "FRANCE",
    129 -> "EAST GERMANY",
    130 -> "GERMANY",
    131 -> "GREECE",
    132 -> "HUNGARY",
    133 -> "ITALY",
    134 -> "LUXEMBOURG",
    135 -> "NETHERLANDS",
    136 -> "NORWAY",
    137 -> "POLAND",
    138 -> "PORTUGAL",
    139 -> "ROMANIA",
    140 -> "SPAIN",
    141 -> "SWEDEN",
    142 -> "SWITZERLAND",
    143 -> "TURKEY",
    144 -> "USSR",
    145 -> "YUGOSLAVIA",
    147 -> "MONACO",
    148 -> "ALBANIA",
    149 -> "CYPRUS",
    150 -> "ICELAND",
    151 -> "LIECHTENSTEIN",
    152 -> "FAROE ISLANDS",
    153 -> "SAN MARINO",
    154 -> "VATICAN CITY STATE",
    155 -> "REP OF BOSNIA-HERZEGOVINA",
    156 -> "REPUBLIC OF CROATIA",
    157 -> "FORMER YUG REP OF MACEDONIA",
    158 -> "REPUBLIC OF SLOVENIA",
    159 -> "FEDERAL REP OF YUGOSLAVIA",
    161 -> "AFGHANISTAN",
    162 -> "ALGERIA",
    163 -> "ANGOLA",
    164 -> "ARGENTINA",
    165 -> "BOLIVIA",
    166 -> "BRAZIL",
    167 -> "KAMPUCHEA",
    168 -> "CAMEROON",
    169 -> "CHILE",
    170 -> "CHINA PEOPLES REPUBLIC",
    171 -> "COLOMBIA",
    172 -> "ZAIRE",
    173 -> "COSTA RICA",
    174 -> "CUBA",
    175 -> "DOMINICAN REPUBLIC",
    176 -> "ECUADOR",
    177 -> "EGYPT",
    178 -> "EL SALVADOR",
    179 -> "ETHIOPIA",
    180 -> "TAIWAN",
    181 -> "GUATEMALA",
    182 -> "HAITI",
    183 -> "BELIZE",
    184 -> "INDONESIA",
    185 -> "IRAN",
    186 -> "IRAQ",
    187 -> "ISRAEL",
    188 -> "COTE D'IVOIRE",
    189 -> "JAPAN",
    190 -> "LEBANON",
    191 -> "LIBERIA",
    192 -> "MACAU",
    193 -> "MALAGASY REPUBLIC",
    194 -> "MEXICO",
    195 -> "MOROCCO",
    196 -> "MOZAMBIQUE",
    197 -> "NEPAL",
    198 -> "ANTILLES (NETHERLANDS)",
    199 -> "NEW CALEDONIA",
    200 -> "NICARAGUA",
    201 -> "PANAMA",
    202 -> "PARAGUAY",
    203 -> "PERU",
    204 -> "PHILIPPINES",
    205 -> "PUERTO RICO",
    206 -> "ZIMBABWE",
    207 -> "SAUDI ARABIA",
    208 -> "SENEGAL",
    209 -> "SOMALIA",
    210 -> "SOUTH KOREA",
    211 -> "VIETNAM",
    212 -> "SUDAN",
    213 -> "SYRIA",
    214 -> "THAILAND",
    215 -> "TOGO",
    216 -> "TUNISIA",
    217 -> "URUGUAY",
    218 -> "USA",
    219 -> "VENEZUELA",
    220 -> "REPUBLIC OF YEMEN",
    222 -> "FRENCH OVERSEAS DEPARTMENT",
    223 -> "GABON",
    224 -> "LAOS",
    225 -> "RWANDA",
    226 -> "CENTRAL AFRICAN REPUBLIC",
    227 -> "DJIBOUTI",
    228 -> "SURINAM",
    229 -> "BURUNDI",
    230 -> "HONDURAS",
    231 -> "GUINEA",
    232 -> "BENIN",
    233 -> "CONGO",
    234 -> "CHAD",
    239 -> "MALI",
    240 -> "MAURITANIA",
    241 -> "NIGER",
    242 -> "CZECH REPUBLIC",
    243 -> "SLOVAK REPUBLIC",
    244 -> "TAHITI",
    245 -> "TRISTAN DA CUHNA",
    246 -> "BURKINA FASO",
    248 -> "ABROAD - NOT KNOWN",
    249 -> "NOT YET RECORDED",
    250 -> "STATELESS",
    251 -> "ARUBA",
    252 -> "BOUVET ISLAND",
    253 -> "COCOS (KEELING) ISLANDS",
    254 -> "CHRISTMAS ISLAND",
    255 -> "WESTERN SAHARA",
    256 -> "ERITREA",
    257 -> "MICRONESIA FEDERATION OF",
    258 -> "FRENCH GUIANA",
    259 -> "GUADELOUPE",
    260 -> "SOUTH GEORGIA AND SOUTH SANDWICH ISLAND",
    261 -> "HEARD ISLAND AND MCDONALD ISLANDS",
    262 -> "BRITISH INDIAN OCEAN TERRITORIES",
    263 -> "MARSHALL ISLANDS",
    264 -> "MYANMAR",
    265 -> "NORTHERN MARIANA ISLANDS",
    266 -> "MARTINIQUE",
    267 -> "NIUE",
    268 -> "FRENCH POLYNESIA",
    269 -> "SAINT PIERRE AND MIQUELON",
    270 -> "PITCAIRN",
    271 -> "PALAU",
    272 -> "REUNION",
    273 -> "SVALBARD AND JAN MAYEN",
    274 -> "FRENCH SOUTHERN TERRITORIES",
    275 -> "TOKELAU",
    276 -> "EAST TIMOR",
    277 -> "UNITED STATES MINOR OUTLYING ISLANDS",
    278 -> "WALLIS AND FUTUNA",
    279 -> "MAYOTTE",
    280 -> "DEMOCRATIC REPUBLIC OF CONGO",
    281 -> "CAMBODIA",
    282 -> "RUSSIA",
    283 -> "REPUBLIC OF MONTENEGRO",
    284 -> "REPUBLIC OF SERBIA",
    285 -> "ANTARCTICA",
    286 -> "REPUBLIC OF KOSOVO"
  )

  private val readsName: Reads[JsObject] =
    (
      (__ \ "nameSequenceNumber").json.copyFrom((__ \ "nameSequenceNumber").json.pick) and
        (__ \ "nameType").json.copyFrom((__ \ "nameType").json.pick) and
        (__ \ "titleType").json.copyFrom((__ \ "titleType").json.pick) and
        (__ \ "firstForename").json.copyFrom((__ \ "firstForename").json.pick) and
        (__ \ "secondForename").json.copyFrom((__ \ "secondForename").json.pick) and
        (__ \ "surname").json.copyFrom((__ \ "surname").json.pick) and
        (__ \ "honours").json.copyFrom((__ \ "honours").json.pick).orElse(doNothing) and
        (__ \ "titleType").json.copyFrom((__ \ "titleType").json.pick)
    ).reduce

  private val readsAllNames: Reads[Seq[JsObject]] = (__ \ "nameList" \ "name").read(Reads.seq(readsName))

  private val readsPreferredName: Reads[JsObject] = readsAllNames.map { seqJsObject =>
    def maxNameType(nameType: Int): Option[JsObject] = seqJsObject
      .filter(jsObject => (jsObject \ "nameType").as[Int] == nameType)
      .maxByOption(jsObject => (jsObject \ "nameSequenceNumber").as[Int])

    maxNameType(NameTypeKnownAs).fold(maxNameType(NameTypeReal).getOrElse(Json.obj()))(
      identity
    )
  }

  private val readsAddress: Reads[JsObject] =
    (
      (__ \ "addressSequenceNumber").json.copyFrom((__ \ "addressSequenceNumber").json.pick) and
        (__ \ "countryCode").json.copyFrom((__ \ "countryCode").json.pick) and
        (__ \ "addressType").json.copyFrom((__ \ "addressType").json.pick) and
        (__ \ "addressStatus").json.copyFrom((__ \ "addressStatus").json.pick) and
        (__ \ "addressStartDate").json.copyFrom((__ \ "addressStartDate").json.pick) and
        (__ \ "addressLine1").json.copyFrom((__ \ "addressLine1").json.pick) and
        (__ \ "addressLine2").json.copyFrom((__ \ "addressLine2").json.pick) and
        (__ \ "addressLine3").json.copyFrom((__ \ "addressLine3").json.pick).orElse(doNothing) and
        (__ \ "addressLine4").json.copyFrom((__ \ "addressLine4").json.pick).orElse(doNothing) and
        (__ \ "addressLine5").json.copyFrom((__ \ "addressLine5").json.pick).orElse(doNothing) and
        (__ \ "addressPostcode").json.copyFrom((__ \ "addressPostcode").json.pick).orElse(doNothing)
    ).reduce

  private val readsAllAddresses: Reads[Seq[JsObject]] = (__ \ "addressList" \ "address").read(Reads.seq(readsAddress))

  private val readsPreferredAddress: Reads[JsObject] = readsAllAddresses.map { seqJsObject =>
    def maxAddressType(addressType: Int): Option[JsObject] = seqJsObject
      .filter(jsObject => (jsObject \ "addressType").as[Int] == addressType)
      .maxByOption(jsObject => (jsObject \ "addressSequenceNumber").as[Int])

    maxAddressType(AddressTypeCorrespondance).fold(maxAddressType(AddressTypeResidential).getOrElse(Json.obj()))(
      identity
    )
  }

  private val readsTitleType: Reads[JsString] =
    Reads {
      case JsNumber(0) => JsSuccess(JsString("NotKnown"))
      case JsNumber(1) => JsSuccess(JsString("Mr"))
      case JsNumber(2) => JsSuccess(JsString("Mrs"))
      case JsNumber(3) => JsSuccess(JsString("Miss"))
      case JsNumber(4) => JsSuccess(JsString("Ms"))
      case JsNumber(5) => JsSuccess(JsString("Dr"))
      case JsNumber(6) => JsSuccess(JsString("Rev"))
      case s           => JsError(s"Invalid json $s")
    }
  private val readsCRNInd: Reads[JsString]    =
    Reads {
      case JsNumber(0) => JsSuccess(JsString("false"))
      case JsNumber(1) => JsSuccess(JsString("true"))
      case s           => JsError(s"Invalid json $s")
    }

  def convertCodeToCountryName(code: Int): String = codeToCountry.get(code) match {
    case Some(country) => country
    case None          => code.toString
  }

  private val readsPreferredNameDetails: Reads[JsObject] =
    readsPreferredName.flatMap { preferredNameJsObject =>
      (
        (__ \ "title").json.put((preferredNameJsObject \ "titleType").as[JsString](readsTitleType)) and
          (__ \ "firstForename").json.put((preferredNameJsObject \ "firstForename").as[JsString]) and
          (__ \ "secondForename").json.put((preferredNameJsObject \ "secondForename").as[JsString]) and
          (__ \ "surname").json.put((preferredNameJsObject \ "surname").as[JsString])
      ).reduce.pipe(addOptionalField(_, "honours", (preferredNameJsObject \ "honours").asOpt[String]))
    }

  private val readsPreferredAddressDetails: Reads[JsObject] =
    readsPreferredAddress.map { preferredAddressJsObject =>

      def addOptional(fieldName: String): JsObject =
        (preferredAddressJsObject \ fieldName).asOpt[String].fold(Json.obj())(s => Json.obj(fieldName -> s))

      val addr = Json.obj(
        "addressLine1"     -> (preferredAddressJsObject \ "addressLine1").as[String],
        "addressLine2"     -> (preferredAddressJsObject \ "addressLine2").as[String],
        "addressStartDate" -> (preferredAddressJsObject \ "addressStartDate").as[String],
        "addressCountry"   -> convertCodeToCountryName((preferredAddressJsObject \ "countryCode").as[Int]),
        "addressType"      -> (preferredAddressJsObject \ "addressType").as[Int]
      ) ++ addOptional("addressLine3")
        ++ addOptional("addressLine4")
        ++ addOptional("addressLine5")
        ++ addOptional("addressPostcode")

      Json.obj(
        "address" -> addr
      )
    }

  val reads: Reads[JsObject] =
    (
      readsPreferredNameDetails and
        (__ \ "dateOfBirth").json.copyFrom((__ \ "details" \ "dateOfBirth").json.pick) and
        (__ \ "nino").json.copyFrom((__ \ "details" \ "nino").json.pick) and
        readsPreferredAddressDetails and
        (__ \ "crnIndicator").json.copyFrom((__ \ "details" \ "crnIndicator").read(readsCRNInd))
    ).reduce
}
