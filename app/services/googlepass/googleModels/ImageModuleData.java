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

public final class ImageModuleData extends GenericJson {
    @Key
    private String id;
    @Key
    private Image mainImage;

    public ImageModuleData() {
    }

    public String getId() {
        return this.id;
    }

    @CanIgnoreReturnValue
    public ImageModuleData setId(String id) {
        this.id = id;
        return this;
    }

    public Image getMainImage() {
        return this.mainImage;
    }

    @CanIgnoreReturnValue
    public ImageModuleData setMainImage(Image mainImage) {
        this.mainImage = mainImage;
        return this;
    }

    public ImageModuleData set(String fieldName, Object value) {
        return (ImageModuleData)super.set(fieldName, value);
    }

    public ImageModuleData clone() {
        return (ImageModuleData)super.clone();
    }
}

