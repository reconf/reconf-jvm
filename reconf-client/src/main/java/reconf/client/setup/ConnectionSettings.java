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
import org.apache.commons.lang.builder.*;
import org.hibernate.validator.constraints.*;


public class ConnectionSettings {

    private String url;
    private Integer timeout = 20;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    @URL(message="{ConnectionSettings.url.error}") @NotNull(message="{ConnectionSettings.url.error}")
    @NotBlank(message="{ConnectionSettings.url.error}")
    @Size(min=1,message="{ConnectionSettings.url.error}")
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    @NotNull(message="{ConnectionSettings.timeout.error}")
    @Min(value=1,message="{ConnectionSettings.timeout.error}")
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @NotNull(message="{ConnectionSettings.timeUnit.null}")
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
        .append("url", getUrl())
        .append("timeout", getTimeout())
        .append("timeUnit", getTimeUnit())
        .toString();
    }
}
