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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.ibm.ws.massive.sa.client.JSONIgnore;

public class Asset extends AbstractJSON {

    private String _id = null;
    private String name = null;
    private String description = null;
    private String shortDescription = null;
    private Type type = null;
    private Calendar createdOn = null;
    private User createdBy = null;
    private Calendar lastUpdatedOn = null;
    private List<Attachment> attachments = null;
    private Provider provider = null;
    private Featured featured = null;
    private State state = null;
    private Feedback feedback = null;
    private AssetInformation information = null;
    private LicenseType licenseType = null;
    private String marketplaceId = null;
    private String marketplaceName = null;
    private String inMyStore = null;
    private PublishedInformation published = null;
    private Privacy privacy;
    private String version = null;
    private WlpInformation wlpInformation;
    private Reviewed reviewed;
    private String licenseId;

    public enum Privacy {
        PUBLIC, PRIVATE
    }

    public enum LicenseType {
        IPLA, ILAN, ILAE, ILAR, UNSPECIFIED
    }

    public enum Type {
        PRODUCTSAMPLE("com.ibm.websphere.ProductSample"),
        OPENSOURCE("com.ibm.websphere.OpenSource"),
        INSTALL("com.ibm.websphere.Install"),
        ADDON("com.ibm.websphere.Addon"),
        FEATURE("com.ibm.websphere.Feature"),
        IFIX("com.ibm.websphere.Ifix"),
        ADMINSCRIPT("com.ibm.websphere.AdminScript"),
        CONFIGSNIPPET("com.ibm.websphere.ConfigSnippet"),
        TOOL("com.ibm.websphere.Tool");

        private final String _type;

        Type(String type) {
            _type = type;
        }

        public String getValue() {
            return _type;
        }

        public static Type forValue(String value) {
            for (Type ty : Type.values()) {
                if (ty.getValue().equals(value)) {
                    return ty;
                }
            }
            return null;
        }
    }

    public enum Featured {
        YES("yes"), NO("no");
        private final String featured;

        private Featured(String featured) {
            this.featured = featured;
        }

        public String getValue() {
            return featured;
        }

        public static Featured forValue(String value) {
            for (Featured featured : Featured.values()) {
                if (featured.getValue().equals(value)) {
                    return featured;
                }
            }
            return null;
        }
    }

    public enum State {
        DRAFT("draft"), AWAITING_APPROVAL("awaiting_approval"), NEED_MORE_INFO("need_more_info"), PUBLISHED("published");
        private final String state;

        private State(String state) {
            this.state = state;
        }

        public String getValue() {
            return state;
        }

        public static State forValue(String value) {
            for (State state : State.values()) {
                if (state.getValue().equals(value)) {
                    return state;
                }
            }
            return null;
        }
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Asset.Type getType() {
        return type;
    }

    public void setType(Asset.Type type) {
        this.type = type;
    }

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Calendar getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Calendar lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    @JSONIgnore
    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void addAttachement(Attachment attachment) {
        // associate the attachment with this asset
        if (attachments == null) {
            attachments = new ArrayList<Attachment>();
        }
        attachments.add(attachment);
    }

    public void setAttachments(List<Attachment> attachments) {
        // blow away old attachments if they exist
        this.attachments = null;

        // Make sure the IDs are set correctly
        for (Attachment attachment : attachments) {
            addAttachement(attachment);
        }
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Asset.Featured getFeatured() {
        return featured;
    }

    public void setFeatured(Asset.Featured featured) {
        this.featured = featured;
    }

    public Asset.State getState() {
        return state;
    }

    public void setState(Asset.State state) {
        this.state = state;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public AssetInformation getInformation() {
        return information;
    }

    public void setInformation(AssetInformation information) {
        this.information = information;
    }

    public void setLicenseType(LicenseType lt) {
        this.licenseType = lt;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseId(String s) {
        this.licenseId = s;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getMarketplaceName() {
        return marketplaceName;
    }

    public void setMarketplaceName(String marketplaceName) {
        this.marketplaceName = marketplaceName;
    }

    public String getInMyStore() {
        return inMyStore;
    }

    public void setInMyStore(String inMyStore) {
        this.inMyStore = inMyStore;
    }

    public PublishedInformation getPublished() {
        return published;
    }

    public void setPublished(PublishedInformation published) {
        this.published = published;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setWlpInformation(WlpInformation wlpInformation) {
        this.wlpInformation = wlpInformation;
    }

    public WlpInformation getWlpInformation() {
        return this.wlpInformation;
    }

    public Reviewed getReviewed() {
        return reviewed;
    }

    public void setReviewed(Reviewed reviewed) {
        this.reviewed = reviewed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((licenseType == null) ? 0 : licenseType.hashCode());
        result = prime * result + ((provider == null) ? 0 : provider.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((wlpInformation == null) ? 0 : wlpInformation.hashCode());

        // name is not immutable for features but is useful for other types
        // (providesFeature from WlpInfo is immutable for features)
        if ((type != null) && (type != Type.FEATURE)) {
            result = prime * result + ((name == null) ? 0 : name.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!equivalent(obj)) {
            return false;
        }
        // If the other object wasn't an asset then we'd have
        // returned false from the equivalent method
        Asset other = (Asset) obj;

        // Now check the fields that are set by massive, and are not
        // used in the equivalent check
        if (_id == null) {
            if (other._id != null)
                return false;
        } else if (!_id.equals(other._id))
            return false;

        if (createdBy == null) {
            if (other.createdBy != null)
                return false;
        } else if (!createdBy.equals(other.createdBy))
            return false;

        if (createdOn == null) {
            if (other.createdOn != null)
                return false;
        } else if (!createdOn.equals(other.createdOn))
            return false;

        if (lastUpdatedOn == null) {
            if (other.lastUpdatedOn != null)
                return false;
        } else if (!lastUpdatedOn.equals(other.lastUpdatedOn))
            return false;

        if (marketplaceId == null) {
            if (other.marketplaceId != null)
                return false;
        } else if (!marketplaceId.equals(other.marketplaceId))
            return false;

        if (marketplaceName == null) {
            if (other.marketplaceName != null)
                return false;
        } else if (!marketplaceName.equals(other.marketplaceName))
            return false;

        if (published == null) {
            if (other.published != null)
                return false;
        } else if (!published.equals(other.published))
            return false;

        if (inMyStore == null) {
            if (other.inMyStore != null)
                return false;
        } else if (!inMyStore.equals(other.inMyStore))
            return false;

        if (state == null) {
            if (other.state != null)
                return false;
        } else if (!state.equals(other.state))
            return false;

        if (featured == null) {
            if (other.featured != null)
                return false;
        } else if (!featured.equals(other.featured))
            return false;

        if (attachments == null) {
            if (other.attachments != null)
                return false;
        } else if (!attachments.equals(other.attachments))
            return false;

        return true;
    }

    public boolean equivalentWithoutAttachments(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Asset other = (Asset) obj;

        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;

        if (shortDescription == null) {
            if (other.shortDescription != null)
                return false;
        } else if (!shortDescription.equals(other.shortDescription))
            return false;

        if (feedback == null) {
            if (other.feedback != null)
                return false;
        } else if (!feedback.equals(other.feedback))
            return false;

        if (information == null) {
            if (other.information != null)
                return false;
        } else if (!information.equals(other.information))
            return false;

        if (licenseType == null) {
            if (other.licenseType != null)
                return false;
        } else if (!licenseType.equals(other.licenseType))
            return false;

        if (licenseId == null) {
            if (other.licenseId != null) {
                return false;
            }
        } else if (!licenseId.equals(other.licenseId)) {
            return false;
        }

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;

        if (privacy == null) {
            if (other.privacy != null)
                return false;
        } else if (!privacy.equals(other.privacy))
            return false;

        if (provider == null) {
            if (other.provider != null)
                return false;
        } else if (!provider.equals(other.provider))
            return false;

        if (reviewed == null) {
            if (other.reviewed != null)
                return false;
        } else if (!reviewed.equals(other.reviewed))
            return false;

        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;

        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;

        if (wlpInformation == null) {
            if (other.wlpInformation != null)
                return false;
        } else if (!wlpInformation.equivalent(other.wlpInformation))
            return false;

        return true;
    }

    public boolean equivalent(Object obj) {
        if (!equivalentWithoutAttachments(obj)) {
            return false;
        }
        // If the other object wasn't an asset then we'd have
        // returned false from the equivalent method
        Asset other = (Asset) obj;

        // Need to call equivalent on attachments
        if (attachments == null) {
            if (other.attachments != null)
                return false;
        } else {
            // Attachments is not null

            // Check if other attachments are null
            if (other.attachments == null) {
                return false;
            } else {

                if (other.attachments.size() != attachments.size()) {
                    return false;
                }

                // Check attachment contents
                outer: for (Attachment at : attachments) {
                    // try and find equivalent attachment in other asset
                    for (Attachment otherAt : other.attachments) {
                        if (at.equivalent(otherAt)) {
                            // Found a mactch, move to next attachment
                            continue outer;
                        }
                    }
                    // Didn't find a match
                    return false;
                }
            }
        }

        return true;
    }

}
