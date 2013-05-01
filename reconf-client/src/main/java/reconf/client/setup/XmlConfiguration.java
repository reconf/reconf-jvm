/*
 *    Copyright 1996-2013 UOL Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package reconf.client.setup;

import javax.validation.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.*;
import org.apache.commons.lang.builder.*;
import reconf.client.elements.*;

@XmlRootElement(name="configuration")
public class XmlConfiguration {

    private LocalCacheSettings localCacheSettings;
    private ConnectionSettings connectionSettings;
    private ReloadPolicyElement annotationOverride;

    @XmlElement(name="local-cache") @Valid @NotNull
    public LocalCacheSettings getLocalCacheSettings() {
        return localCacheSettings;
    }
    public void setLocalCacheSettings(LocalCacheSettings localCacheSettings) {
        this.localCacheSettings = localCacheSettings;
    }

    @XmlElement(name="server") @Valid @NotNull
    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    @XmlElement(name="override-reload-policy") @Valid
    public ReloadPolicyElement getAnnotationOverride() {
        return annotationOverride;
    }
    public void setAnnotationOverride(ReloadPolicyElement annotationOverride) {
        this.annotationOverride = annotationOverride;
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("local-cache", getLocalCacheSettings())
        .append("server", getConnectionSettings());
        if (getAnnotationOverride() != null) {
            result.append("override-reload-policy", getAnnotationOverride());
        }
        return result.toString();
    }
}
