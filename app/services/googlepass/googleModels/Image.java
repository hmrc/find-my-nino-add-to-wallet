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

public final class Image extends GenericJson {
    @Key
    private LocalizedString contentDescription;
    @Key
    private String kind;
    @Key
    private ImageUri sourceUri;

    public Image() {
    }

    public LocalizedString getContentDescription() {
        return this.contentDescription;
    }

    @CanIgnoreReturnValue
    public Image setContentDescription(LocalizedString contentDescription) {
        this.contentDescription = contentDescription;
        return this;
    }

    public String getKind() {
        return this.kind;
    }

    @CanIgnoreReturnValue
    public Image setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public ImageUri getSourceUri() {
        return this.sourceUri;
    }

    @CanIgnoreReturnValue
    public Image setSourceUri(ImageUri sourceUri) {
        this.sourceUri = sourceUri;
        return this;
    }

    public Image set(String fieldName, Object value) {
        return (Image)super.set(fieldName, value);
    }

    public Image clone() {
        return (Image)super.clone();
    }
}

