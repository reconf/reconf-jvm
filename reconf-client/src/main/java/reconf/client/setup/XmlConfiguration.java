/*
 *    Copyright 2013-2015 ReConf Team
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class XmlConfiguration {

    private LocalCacheSettings localCacheSettings;
    private ConnectionSettings connectionSettings;
    private GlobalPollingFrequencySettings annotationOverride;
    private boolean experimentalFeatures;
    private boolean debug;

    public LocalCacheSettings getLocalCacheSettings() {
        return localCacheSettings;
    }
    public void setLocalCacheSettings(LocalCacheSettings localCacheSettings) {
        this.localCacheSettings = localCacheSettings;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    public GlobalPollingFrequencySettings getAnnotationOverride() {
        return annotationOverride;
    }
    public void setAnnotationOverride(GlobalPollingFrequencySettings annotationOverride) {
        this.annotationOverride = annotationOverride;
    }

    public boolean isExperimentalFeatures() {
        return experimentalFeatures;
    }
    public void setExperimentalFeatures(boolean experimentalFeatures) {
        this.experimentalFeatures = experimentalFeatures;
    }

    public boolean isDebug() {
        return debug;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("local-cache", getLocalCacheSettings())
        .append("server", getConnectionSettings());
        if (getAnnotationOverride() != null) {
            result.append("global-polling-frequency", getAnnotationOverride());
        }
        if (experimentalFeatures) {
            result.append("experimental-features", experimentalFeatures);
        }
        if (debug) {
            result.append("debug", debug);
        }
        return result.toString();
    }
}
