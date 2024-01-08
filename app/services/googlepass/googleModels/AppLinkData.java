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

public final class AppLinkData extends GenericJson {
    @Key
    private AppLinkDataAppLinkInfo androidAppLinkInfo;
    @Key
    private AppLinkDataAppLinkInfo iosAppLinkInfo;
    @Key
    private AppLinkDataAppLinkInfo webAppLinkInfo;

    public AppLinkData() {
    }

    public AppLinkDataAppLinkInfo getAndroidAppLinkInfo() {
        return this.androidAppLinkInfo;
    }

    @CanIgnoreReturnValue
    public AppLinkData setAndroidAppLinkInfo(AppLinkDataAppLinkInfo androidAppLinkInfo) {
        this.androidAppLinkInfo = androidAppLinkInfo;
        return this;
    }

    public AppLinkDataAppLinkInfo getIosAppLinkInfo() {
        return this.iosAppLinkInfo;
    }

    @CanIgnoreReturnValue
    public AppLinkData setIosAppLinkInfo(AppLinkDataAppLinkInfo iosAppLinkInfo) {
        this.iosAppLinkInfo = iosAppLinkInfo;
        return this;
    }

    public AppLinkDataAppLinkInfo getWebAppLinkInfo() {
        return this.webAppLinkInfo;
    }

    @CanIgnoreReturnValue
    public AppLinkData setWebAppLinkInfo(AppLinkDataAppLinkInfo webAppLinkInfo) {
        this.webAppLinkInfo = webAppLinkInfo;
        return this;
    }

    public AppLinkData set(String fieldName, Object value) {
        return (AppLinkData)super.set(fieldName, value);
    }

    public AppLinkData clone() {
        return (AppLinkData)super.clone();
    }
}

