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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bowen.cbw
 * @since 9/3/2014.
 */
public class DefaultDialectResolver implements IDialectResolver {

    static final Logger LOG = LoggerFactory.getLogger(DefaultDialectResolver.class);

    /**
     * resolver ISO 639-1 id
     * zh_CN
     * en
     * @param localeInfo
     * @return
     */
    @Override
    public Dialect resolve(String localeInfo) {

        if (null != localeInfo) {
            localeInfo = localeInfo.trim();
            if (localeInfo.startsWith("zh")
                    || localeInfo.startsWith("ZH")) {

                if (localeInfo.length() > 4) {
                    char c3 = localeInfo.charAt(3);
                    if ((c3 == 'T' || c3 == 't')
                            || (c3 == 'H' || c3 == 'h')) {
                        return Dialect.TraditionalChinese;
                    }
                }
                return Dialect.SimplifiedChinese;

            } else if (localeInfo.startsWith("en")
                    || localeInfo.startsWith("EN")) {
                return Dialect.English;
            } else if (localeInfo.length() > 2) {
                return Dialect.parseISO639_1(localeInfo.substring(0, 2));
            }
        }
        LOG.warn("failed to resolver dialect [" + localeInfo + "]. set as Unknown");
        return Dialect.Unknown;
    }

}
