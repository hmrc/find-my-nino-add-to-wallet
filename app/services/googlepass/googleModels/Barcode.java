/*
 * Copyright 2024 HM Revenue & Customs
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

public final class Barcode extends GenericJson {
    @Key
    private String alternateText;
    @Key
    private String kind;
    @Key
    private String renderEncoding;
    @Key
    private LocalizedString showCodeText;
    @Key
    private String type;
    @Key
    private String value;

    public Barcode() {
    }

    public String getAlternateText() {
        return this.alternateText;
    }

    @CanIgnoreReturnValue
    public Barcode setAlternateText(String alternateText) {
        this.alternateText = alternateText;
        return this;
    }

    public String getKind() {
        return this.kind;
    }

    @CanIgnoreReturnValue
    public Barcode setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public String getRenderEncoding() {
        return this.renderEncoding;
    }

    @CanIgnoreReturnValue
    public Barcode setRenderEncoding(String renderEncoding) {
        this.renderEncoding = renderEncoding;
        return this;
    }

    public LocalizedString getShowCodeText() {
        return this.showCodeText;
    }

    @CanIgnoreReturnValue
    public Barcode setShowCodeText(LocalizedString showCodeText) {
        this.showCodeText = showCodeText;
        return this;
    }

    public String getType() {
        return this.type;
    }

    @CanIgnoreReturnValue
    public Barcode setType(String type) {
        this.type = type;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    @CanIgnoreReturnValue
    public Barcode setValue(String value) {
        this.value = value;
        return this;
    }

    public Barcode set(String fieldName, Object value) {
        return (Barcode)super.set(fieldName, value);
    }

    public Barcode clone() {
        return (Barcode)super.clone();
    }
}

