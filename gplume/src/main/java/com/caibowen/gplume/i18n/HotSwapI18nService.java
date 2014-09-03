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

import com.caibowen.gplume.common.Pair;
import com.caibowen.gplume.context.FileInputStreamProvider;
import com.caibowen.gplume.context.InputStreamCallback;
import com.caibowen.gplume.context.InputStreamSupport;
import com.caibowen.gplume.context.bean.InitializingBean;
import com.caibowen.gplume.misc.logging.Logger;
import com.caibowen.gplume.misc.logging.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * @author bowen.cbw
 * @since 9/3/2014.
 */
public class HotSwapI18nService extends GenI18nService implements InitializingBean {

    private static final long serialVersionUID = 2823988842476726160L;

    private static final Logger LOG = LoggerFactory.getLogger(HotSwapI18nService.class.getName());

    protected final EnumMap<Dialect, Pair<Long, File>> fileTable
            = new EnumMap<Dialect, Pair<Long, File>>(Dialect.class);

    InputStreamSupport fileStreamSupport = new InputStreamSupport(new FileInputStreamProvider());
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }


    @Override
    protected void loadFiles() throws Exception {

        if (pkgFiles == null || pkgFiles.size() == 0) {
            throw new NullPointerException("empty properties");
        }

        for (Map.Entry<Object, Object> e : pkgFiles.entrySet()) {
            if (e.getKey() instanceof String && e.getValue() instanceof String) {
                String k = (String) e.getKey();
                String path = (String) e.getValue();

                path = super.streamSupport.getStreamProvider().getRealPath(path);
                File file = new File(path);

                if (!file.exists() || !file.isFile() || !file.canRead())
                    throw new IllegalArgumentException(
                            "[" + path + "] is not a file or cannot be read");

                final Dialect dialect = resolver.resolve(k);
                final Properties pkg = new Properties();

                fileStreamSupport.withPath(path, new InputStreamCallback() {
                    @Override
                    public void doInStream(InputStream stream) throws Exception {
                        pkg.load(stream);
                    }
                });
                addProperties(dialect, pkg);
                fileTable.put(dialect, new Pair<Long, File>(file.lastModified(), file));
            }// if is string
        }// for each pkg

    }



    @Override
    @Nullable
    public NativePackage getPkg(Dialect lang) {
        if (lang == Dialect.Unknown)
            lang = defaultLang;

        update(lang);
        return pkgTable.get(lang);
    }


    public NativePackage getDefaultPkg() {
        update(defaultLang);
        return pkgTable.get(defaultLang);
    }

    protected void update(Dialect lang) {

        final Pair<Long, File> disk = fileTable.get(lang);
        final long lm = disk.second.lastModified();
        if (lm > disk.first) {
            final Properties p = new Properties();
            fileStreamSupport.withPath(disk.second.getPath(), new InputStreamCallback() {
                @Override
                public void doInStream(InputStream stream) throws Exception {
                    p.load(stream);
                }
            });
            addProperties(lang, p);
            disk.first = lm;

            PkgChangedEvent e = new PkgChangedEvent(getPkg(lang), this);
        }
    }

}
