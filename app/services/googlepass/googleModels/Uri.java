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

public final class Uri extends GenericJson {
    @Key
    private String description;
    @Key
    private String id;
    @Key
    private String kind;
    @Key
    private LocalizedString localizedDescription;
    @Key
    private String uri;

    public Uri() {
    }

    public String getDescription() {
        return this.description;
    }

    @CanIgnoreReturnValue
    public Uri setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getId() {
        return this.id;
    }

    @CanIgnoreReturnValue
    public Uri setId(String id) {
        this.id = id;
        return this;
    }

    public String getKind() {
        return this.kind;
    }

    @CanIgnoreReturnValue
    public Uri setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public LocalizedString getLocalizedDescription() {
        return this.localizedDescription;
    }

    @CanIgnoreReturnValue
    public Uri setLocalizedDescription(LocalizedString localizedDescription) {
        this.localizedDescription = localizedDescription;
        return this;
    }

    public String getUri() {
        return this.uri;
    }

    @CanIgnoreReturnValue
    public Uri setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Uri set(String fieldName, Object value) {
        return (Uri)super.set(fieldName, value);
    }

    public Uri clone() {
        return (Uri)super.clone();
    }
}

