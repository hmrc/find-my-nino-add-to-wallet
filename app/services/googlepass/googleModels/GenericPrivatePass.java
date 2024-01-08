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
import com.google.api.client.util.Data;
import com.google.api.client.util.Key;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.List;

public final class GenericPrivatePass extends GenericJson {
    @Key
    private AppLinkData appLinkData;
    @Key
    private Barcode barcode;
    @Key
    private GroupingInfo groupingInfo;
    @Key
    private LocalizedString header;
    @Key
    private Image headerLogo;
    @Key
    private Image heroImage;
    @Key
    private String hexBackgroundColor;
    @Key
    private String id;
    @Key
    private List<ImageModuleData> imageModulesData;
    @Key
    private LinksModuleData linksModuleData;
    @Key
    private LocalizedString metaText;
    @Key
    private List<TextModuleData> textModulesData;
    @Key
    private LocalizedString title;
    @Key
    private LocalizedString titleLabel;
    @Key
    private String type;
    @Key
    private TimeInterval validTimeInterval;

    public GenericPrivatePass() {
    }

    public AppLinkData getAppLinkData() {
        return this.appLinkData;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setAppLinkData(AppLinkData appLinkData) {
        this.appLinkData = appLinkData;
        return this;
    }

    public Barcode getBarcode() {
        return this.barcode;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setBarcode(Barcode barcode) {
        this.barcode = barcode;
        return this;
    }

    public GroupingInfo getGroupingInfo() {
        return this.groupingInfo;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setGroupingInfo(GroupingInfo groupingInfo) {
        this.groupingInfo = groupingInfo;
        return this;
    }

    public LocalizedString getHeader() {
        return this.header;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setHeader(LocalizedString header) {
        this.header = header;
        return this;
    }

    public Image getHeaderLogo() {
        return this.headerLogo;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setHeaderLogo(Image headerLogo) {
        this.headerLogo = headerLogo;
        return this;
    }

    public Image getHeroImage() {
        return this.heroImage;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setHeroImage(Image heroImage) {
        this.heroImage = heroImage;
        return this;
    }

    public String getHexBackgroundColor() {
        return this.hexBackgroundColor;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setHexBackgroundColor(String hexBackgroundColor) {
        this.hexBackgroundColor = hexBackgroundColor;
        return this;
    }

    public String getId() {
        return this.id;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setId(String id) {
        this.id = id;
        return this;
    }

    public List<ImageModuleData> getImageModulesData() {
        return this.imageModulesData;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setImageModulesData(List<ImageModuleData> imageModulesData) {
        this.imageModulesData = imageModulesData;
        return this;
    }

    public LinksModuleData getLinksModuleData() {
        return this.linksModuleData;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setLinksModuleData(LinksModuleData linksModuleData) {
        this.linksModuleData = linksModuleData;
        return this;
    }

    public LocalizedString getMetaText() {
        return this.metaText;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setMetaText(LocalizedString metaText) {
        this.metaText = metaText;
        return this;
    }

    public List<TextModuleData> getTextModulesData() {
        return this.textModulesData;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setTextModulesData(List<TextModuleData> textModulesData) {
        this.textModulesData = textModulesData;
        return this;
    }

    public LocalizedString getTitle() {
        return this.title;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setTitle(LocalizedString title) {
        this.title = title;
        return this;
    }

    public LocalizedString getTitleLabel() {
        return this.titleLabel;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setTitleLabel(LocalizedString titleLabel) {
        this.titleLabel = titleLabel;
        return this;
    }

    public String getType() {
        return this.type;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setType(String type) {
        this.type = type;
        return this;
    }

    public TimeInterval getValidTimeInterval() {
        return this.validTimeInterval;
    }

    @CanIgnoreReturnValue
    public GenericPrivatePass setValidTimeInterval(TimeInterval validTimeInterval) {
        this.validTimeInterval = validTimeInterval;
        return this;
    }

    public GenericPrivatePass set(String fieldName, Object value) {
        return (GenericPrivatePass)super.set(fieldName, value);
    }

    public GenericPrivatePass clone() {
        return (GenericPrivatePass)super.clone();
    }

    static {
        Data.nullOf(ImageModuleData.class);
        Data.nullOf(TextModuleData.class);
    }
}
