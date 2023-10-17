/*
 * Copyright 2023 HM Revenue & Customs
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

package models

import play.api.libs.json.{Json, OFormat}

case class NameParts(givenName: List[String], familyName: String)
case class Name(nameParts : NameParts)
case class SocialSecurityRecord(personalNumber : String)
case class CredentialSubject(name: Name, socialSecurityNumber: SocialSecurityRecord)
case class VCDocument(`type`: List[String], credentialSubject: CredentialSubject)

case class GovUKVCDocument(sub: String, nbf: Int, iss: String, exp: Int, iat: Int, vc: VCDocument)

case object GovUKVCDocument {
  implicit val namePartsFormat: OFormat[NameParts] = Json.format[NameParts]
  implicit val nameFormat: OFormat[Name] = Json.format[Name]
  implicit val socialSecurityRecordFormat: OFormat[SocialSecurityRecord] = Json.format[SocialSecurityRecord]
  implicit val credentialSubjectFormat: OFormat[CredentialSubject] = Json.format[CredentialSubject]
  implicit val vcDocumentFormat: OFormat[VCDocument] = Json.format[VCDocument]
  implicit val govUKVCDocumentFormat: OFormat[GovUKVCDocument] = Json.format[GovUKVCDocument]
}
