/*
 *    Copyright 1996-2014 UOL Inc
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
import org.apache.commons.lang.builder.*;

public class GlobalUpdateFrequencySettings {

    private Integer interval;
    private TimeUnit timeUnit;

    public Integer getInterval() {
        return interval;
    }
    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
        .append("interval", getInterval())
        .append("time-unit", getTimeUnit())
        .toString();
    }

}
