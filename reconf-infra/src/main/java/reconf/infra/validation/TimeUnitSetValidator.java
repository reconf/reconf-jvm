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
package reconf.infra.validation;

import java.util.*;
import java.util.concurrent.*;
import javax.validation.*;


public class TimeUnitSetValidator implements ConstraintValidator<TimeUnitSet, TimeUnit> {

    private Set<TimeUnit> allowed;

    @Override
    public void initialize(TimeUnitSet constraintAnnotation) {
        allowed = new HashSet<TimeUnit>(Arrays.asList(constraintAnnotation.allowed()));
    }

    @Override
    public boolean isValid(TimeUnit value, ConstraintValidatorContext context) {
        return allowed.contains(value);
    }

}
