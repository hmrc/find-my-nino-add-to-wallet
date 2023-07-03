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

public final class TextModuleData extends GenericJson {
    @Key
    private String body;
    @Key
    private String header;
    @Key
    private String id;
    @Key
    private LocalizedString localizedBody;
    @Key
    private LocalizedString localizedHeader;

    public TextModuleData() {
    }

    public String getBody() {
        return this.body;
    }

    @CanIgnoreReturnValue
    public TextModuleData setBody(String body) {
        this.body = body;
        return this;
    }

    public String getHeader() {
        return this.header;
    }

    @CanIgnoreReturnValue
    public TextModuleData setHeader(String header) {
        this.header = header;
        return this;
    }

    public String getId() {
        return this.id;
    }

    @CanIgnoreReturnValue
    public TextModuleData setId(String id) {
        this.id = id;
        return this;
    }

    public LocalizedString getLocalizedBody() {
        return this.localizedBody;
    }

    @CanIgnoreReturnValue
    public TextModuleData setLocalizedBody(LocalizedString localizedBody) {
        this.localizedBody = localizedBody;
        return this;
    }

    public LocalizedString getLocalizedHeader() {
        return this.localizedHeader;
    }

    @CanIgnoreReturnValue
    public TextModuleData setLocalizedHeader(LocalizedString localizedHeader) {
        this.localizedHeader = localizedHeader;
        return this;
    }

    public TextModuleData set(String fieldName, Object value) {
        return (TextModuleData)super.set(fieldName, value);
    }

    public TextModuleData clone() {
        return (TextModuleData)super.clone();
    }
}

