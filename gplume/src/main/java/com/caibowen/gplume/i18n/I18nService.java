/*
 * *****************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.i18n;

import com.caibowen.gplume.context.bean.InitializingBean;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Properties;
import java.util.Set;

/**
 * @author bowen.cbw
 * @since 9/3/2014.
 */
public interface I18nService extends IDialectResolver, InitializingBean {

    /**
     * bean id for internationalization
     */
    static final String BEAN_ID = "i18nService";

    void afterPropertiesSet() throws Exception;


    @Inject
    void setPkgFiles(Properties pkgFiles);

    @Nullable
    NativePackage getPkg(String localeInfo);

    @Nullable
    NativePackage getPkg(Dialect lang);

    Set<Dialect> getSupportedDialects();

    Dialect getDefaultLang();

    NativePackage getDefaultPkg();

    void setDefaultLang(Dialect defaultLang);

    void setDefaultTimeZone(String timeZoneId);

    void setResolver(IDialectResolver resolver);

}
