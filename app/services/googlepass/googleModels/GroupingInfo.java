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

public final class GroupingInfo extends GenericJson {
    @Key
    private String groupingId;
    @Key
    private Integer sortIndex;

    public GroupingInfo() {
    }

    public String getGroupingId() {
        return this.groupingId;
    }

    @CanIgnoreReturnValue
    public GroupingInfo setGroupingId(String groupingId) {
        this.groupingId = groupingId;
        return this;
    }

    public Integer getSortIndex() {
        return this.sortIndex;
    }

    @CanIgnoreReturnValue
    public GroupingInfo setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
        return this;
    }

    public GroupingInfo set(String fieldName, Object value) {
        return (GroupingInfo)super.set(fieldName, value);
    }

    public GroupingInfo clone() {
        return (GroupingInfo)super.clone();
    }
}

