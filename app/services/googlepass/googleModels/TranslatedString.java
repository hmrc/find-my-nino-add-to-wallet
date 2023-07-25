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

package services.googlepass.googleModels;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

public final class TranslatedString extends GenericJson {
    @Key
    private String kind;
    @Key
    private String language;
    @Key
    private String value;

    public TranslatedString() {
    }

    public String getKind() {
        return this.kind;
    }

    @CanIgnoreReturnValue
    public TranslatedString setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public String getLanguage() {
        return this.language;
    }

    @CanIgnoreReturnValue
    public TranslatedString setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    @CanIgnoreReturnValue
    public TranslatedString setValue(String value) {
        this.value = value;
        return this;
    }

    public TranslatedString set(String fieldName, Object value) {
        return (TranslatedString)super.set(fieldName, value);
    }

    public TranslatedString clone() {
        return (TranslatedString)super.clone();
    }
}

