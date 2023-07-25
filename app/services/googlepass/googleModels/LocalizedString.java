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

import java.util.List;

public final class LocalizedString extends GenericJson {
    @Key
    private TranslatedString defaultValue;
    @Key
    private String kind;
    @Key
    private List<TranslatedString> translatedValues;

    public LocalizedString() {
    }

    public TranslatedString getDefaultValue() {
        return this.defaultValue;
    }

    @CanIgnoreReturnValue
    public LocalizedString setDefaultValue(TranslatedString defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getKind() {
        return this.kind;
    }

    @CanIgnoreReturnValue
    public LocalizedString setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public List<TranslatedString> getTranslatedValues() {
        return this.translatedValues;
    }

    @CanIgnoreReturnValue
    public LocalizedString setTranslatedValues(List<TranslatedString> translatedValues) {
        this.translatedValues = translatedValues;
        return this;
    }

    public LocalizedString set(String fieldName, Object value) {
        return (LocalizedString)super.set(fieldName, value);
    }

    public LocalizedString clone() {
        return (LocalizedString)super.clone();
    }
}

