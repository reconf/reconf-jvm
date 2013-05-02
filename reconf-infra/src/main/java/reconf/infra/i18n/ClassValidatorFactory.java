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
package reconf.infra.i18n;

import java.util.*;
import javax.validation.*;
import org.hibernate.validator.messageinterpolation.*;
import org.hibernate.validator.resourceloading.*;


public class ClassValidatorFactory implements ResourceBundleLocator {

    private BundleSettings settings;

    private ClassValidatorFactory(Class<?> cls) {
        settings = new BundleSettings(cls);
    }

    @Override
    public ResourceBundle getResourceBundle(Locale ignored) {
        return ResourceBundle.getBundle(settings.getBundleResourceName(), LocaleHolder.value());
    }

    public static Validator create(Class<?> cls) {
        Configuration<?> configure = getConfiguration();
        configure.messageInterpolator(new ResourceBundleMessageInterpolator(new ClassValidatorFactory(cls)));
        return buildIt(configure).getValidator();
    }

    private static Configuration<?> getConfiguration() {
        return Validation.byDefaultProvider().configure();
    }

    private static ValidatorFactory buildIt(Configuration<?> configure) {
        return configure.buildValidatorFactory();
    }
}
