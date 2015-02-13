/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ibm.ws.massive.sa.client.model;

import java.util.List;

public class AppliesToFilterInfo {
    String productId;
    FilterVersion minVersion;
    FilterVersion maxVersion;
    List<String> editions;
    String installType;
    List<String> rawEditions;

    /**
     * Indicates if this class has a max version. Used so that we can filter assets that don't and does not have a setter but is set by a call to
     * {@link #setMaxVersion(FilterVersion)}. This is a string that stores a boolean as you can only filter on Strings in Massive.
     *
     * @return the hasMaxVersion
     */
    public String getHasMaxVersion() {
        if (maxVersion == null) {
            return Boolean.toString(false);
        } else {
            return Boolean.toString(true);
        }
    }

    public String getInstallType() {
        return installType;
    }

    public void setInstallType(String installType) {
        this.installType = installType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public FilterVersion getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(FilterVersion minVersion) {
        this.minVersion = minVersion;
    }

    public FilterVersion getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(FilterVersion maxVersion) {
        this.maxVersion = maxVersion;
    }

    public List<String> getEditions() {
        return editions;
    }

    public void setEditions(List<String> edition) {
        this.editions = edition;
    }

    public List<String> getRawEditions() {
        return rawEditions;
    }

    public void setRawEditions(List<String> rawEditions) {
        this.rawEditions = rawEditions;
    }

    @Override
    public String toString() {
        return "[AppliesToFilterInfo <productId=" + productId + "> <minVersion=" + minVersion + "> <maxVersion=" +
               maxVersion + "> <editions=" + editions + "> <installType=" + installType + ">]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((editions == null) ? 0 : editions.hashCode());
        result = prime * result
                 + ((maxVersion == null) ? 0 : maxVersion.hashCode());
        result = prime * result
                 + ((minVersion == null) ? 0 : minVersion.hashCode());
        result = prime * result
                 + ((productId == null) ? 0 : productId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppliesToFilterInfo other = (AppliesToFilterInfo) obj;
        if (editions == null) {
            if (other.editions != null)
                return false;
        } else if (!editions.equals(other.editions))
            return false;
        if (rawEditions == null) {
            if (other.rawEditions != null)
                return false;
        } else if (!rawEditions.equals(other.rawEditions))
            return false;
        if (maxVersion == null) {
            if (other.maxVersion != null)
                return false;
        } else if (!maxVersion.equals(other.maxVersion))
            return false;
        if (minVersion == null) {
            if (other.minVersion != null)
                return false;
        } else if (!minVersion.equals(other.minVersion))
            return false;
        if (productId == null) {
            if (other.productId != null)
                return false;
        } else if (!productId.equals(other.productId))
            return false;
        if (installType == null) {
            if (other.installType != null)
                return false;
        } else if (!installType.equals(other.installType))
            return false;
        return true;
    }
}
