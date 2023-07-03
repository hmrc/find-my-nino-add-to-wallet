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

public final class AppLinkDataAppLinkInfo extends GenericJson {
    @Key
    private Image appLogoImage;
    @Key
    private AppLinkDataAppLinkInfoAppTarget appTarget;
    @Key
    private LocalizedString description;
    @Key
    private LocalizedString title;

    public AppLinkDataAppLinkInfo() {
    }

    public Image getAppLogoImage() {
        return this.appLogoImage;
    }

    @CanIgnoreReturnValue
    public AppLinkDataAppLinkInfo setAppLogoImage(Image appLogoImage) {
        this.appLogoImage = appLogoImage;
        return this;
    }

    public AppLinkDataAppLinkInfoAppTarget getAppTarget() {
        return this.appTarget;
    }

    @CanIgnoreReturnValue
    public AppLinkDataAppLinkInfo setAppTarget(AppLinkDataAppLinkInfoAppTarget appTarget) {
        this.appTarget = appTarget;
        return this;
    }

    public LocalizedString getDescription() {
        return this.description;
    }

    @CanIgnoreReturnValue
    public AppLinkDataAppLinkInfo setDescription(LocalizedString description) {
        this.description = description;
        return this;
    }

    public LocalizedString getTitle() {
        return this.title;
    }

    @CanIgnoreReturnValue
    public AppLinkDataAppLinkInfo setTitle(LocalizedString title) {
        this.title = title;
        return this;
    }

    public AppLinkDataAppLinkInfo set(String fieldName, Object value) {
        return (AppLinkDataAppLinkInfo)super.set(fieldName, value);
    }

    public AppLinkDataAppLinkInfo clone() {
        return (AppLinkDataAppLinkInfo)super.clone();
    }
}

