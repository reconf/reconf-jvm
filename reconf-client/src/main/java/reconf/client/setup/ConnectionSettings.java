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

import java.util.concurrent.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.*;
import org.apache.commons.lang.builder.*;
import org.hibernate.validator.constraints.*;

/**
 * The necessary parameters to connect ReConf Server
 */
public class ConnectionSettings {

    private String url;
    private int timeout = 20;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private int maxRetry = 3;

    @URL(message="{setup.ConnectionSettings.url.error}")
    @NotNull(message="{setup.ConnectionSettings.url.error}")
    @NotBlank(message="{setup.ConnectionSettings.url.error}")
    @Size(min=1,message="{setup.ConnectionSettings.url.error}")
    @XmlElement(name="url")
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    @Min(value=1,message="{setup.ConnectionSettings.timeout.error}")
    @XmlElement(name="timeout")
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @NotNull(message="{setup.ConnectionSettings.timeUnit.null}")
    @XmlElement(name="time-unit")
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Min(value=1,message="{setup.ConnectionSettings.retry.error}")
    @XmlElement(name="max-retry")
    public int getMaxRetry() {
        return maxRetry;
    }
    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
        .append("url", getUrl())
        .append("timeout", getTimeout())
        .append("time-unit", getTimeUnit())
        .append("max-retry", getMaxRetry())
        .toString();
    }
}
