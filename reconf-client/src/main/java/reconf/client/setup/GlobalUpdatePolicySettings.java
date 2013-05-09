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
import reconf.client.elements.*;

public class GlobalUpdatePolicySettings extends UpdatePolicyElement {

    @NotNull(message="{setup.GlobalUpdatePolicySettings.interval.error}")
    @Min(value=1,message="{setup.GlobalUpdatePolicySettings.interval.error}")
    @XmlElement(name="interval")
    public Integer getInterval() {
        return super.getInterval();
    }
    public void setInterval(Integer interval) {
        super.setInterval(interval);
    }

    @NotNull(message="{setup.GlobalUpdatePolicySettings.timeUnit.null}")
    @XmlElement(name="time-unit")
    public TimeUnit getTimeUnit() {
        return super.getTimeUnit();
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        super.setTimeUnit(timeUnit);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
        .append("interval", getInterval())
        .append("time-unit", getTimeUnit())
        .toString();
    }

}
