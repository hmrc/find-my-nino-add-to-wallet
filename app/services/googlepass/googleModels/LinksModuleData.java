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
import com.google.api.client.util.Data;
import com.google.api.client.util.Key;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.List;

public final class LinksModuleData extends GenericJson {
    @Key
    private List<Uri> uris;

    public LinksModuleData() {
    }

    public List<Uri> getUris() {
        return this.uris;
    }

    @CanIgnoreReturnValue
    public LinksModuleData setUris(List<Uri> uris) {
        this.uris = uris;
        return this;
    }

    public LinksModuleData set(String fieldName, Object value) {
        return (LinksModuleData)super.set(fieldName, value);
    }

    public LinksModuleData clone() {
        return (LinksModuleData)super.clone();
    }

    static {
        Data.nullOf(Uri.class);
    }
}
