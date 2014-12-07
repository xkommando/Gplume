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

package com.caibowen.gplume.context;

import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.resource.InputStreamCallback;
import com.caibowen.gplume.resource.InputStreamProvider;
import com.caibowen.gplume.resource.InputStreamProviderProxy;
import com.caibowen.gplume.resource.InputStreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * @auther bowen.cbw
 * @since 8/15/2014.
 */
public class ConfigCenter implements Serializable {

    private static final long serialVersionUID = -7772935889089989028L;

    private static final Logger LOG = LoggerFactory.getLogger(ConfigCenter.class);

    protected InputStreamProviderProxy proxy;
    protected InputStreamSupport streamSupport;


    /**
     * record the current working config file.
     * when half, it is pointed at the main config file
     */
    private String currentConfigName;

    /**
     * local config
     */
    private final Map<String, Map<String, String>> configs;

    /**
     * map storing local config key and the file name;
     */
    private final Map<String, String> keyToConfigName;

    private final Map<String, String> globalProperties;

    public ConfigCenter() {
        proxy = InputStreamProviderProxy.DEFAULT_PROXY;
        streamSupport = InputStreamSupport.DEFAULT_SUPPORT;
        globalProperties = new HashMap<>(64);
        keyToConfigName = new HashMap<>(64);
        configs = new HashMap<>(32);
    }


    synchronized public void withPath(String path, InputStreamCallback callback) {
        String _c = currentConfigName;
        currentConfigName = path;
        streamSupport.withPath(path, callback);
        currentConfigName = _c;
    }


    synchronized public void scanXMLElem(Element elem) {

        String _sc = elem.getAttribute(XMLTags.SCOPE);
        boolean isGlobal = ConfigScope.Global.name().equalsIgnoreCase(_sc);

        final String loc = elem.getAttribute(XMLTags.IMPROT).trim();

        if (Str.Utils.notBlank(loc)) {

            String _lastConfig = currentConfigName;
            currentConfigName = loc;

            final Properties p = new Properties();

            withPath(loc, new InputStreamCallback() {
                @Override
                public void doInStream(InputStream stream) throws Exception {
                    if (loc.endsWith(".xml"))
                        p.loadFromXML(stream);
                    else
                        p.load(stream);
                }
            });
            if (isGlobal) {
                for (Map.Entry<?, ?> e : p.entrySet()) {
                    Object nk = e.getKey();
                    Object nv = e.getValue();
                    if (nk instanceof String && nv instanceof String) {
                        addGlobal((String)nk, (String)nv);
                    }
                }
            } else {
                for (Map.Entry<?, ?> e : p.entrySet()) {
                    Object nk = e.getKey();
                    Object nv = e.getValue();
                    if (nk instanceof String && nv instanceof String) {
                            addLocal((String)nk, (String)nv);
                    }
                }
            }
            currentConfigName = _lastConfig;
        }

        NodeList nls = elem.getChildNodes();
        for (int i = 0; i < nls.getLength(); i++) {
            Node nn = nls.item(i);
            if (nn.getNodeType() == Node.ELEMENT_NODE) {
                Element ne = (Element) nn;
                String k = ne.getTagName().trim();
                String v = ne.getTextContent().trim();
                if (isGlobal)
                    addGlobal(k, v);
                else
                    addLocal(k, v);
            }
        }
    }

    public void addLocal(String k, String v) {
        Map<String, String> current = configs.get(currentConfigName);

        if (current == null) {
            current = new HashMap(64);
            configs.put(currentConfigName, current);
            current.put(k, v);
            keyToConfigName.put(k, currentConfigName);
            LOG.debug("config file [" + currentConfigName + "] add [" + k + "] -> [" + v + "]");
        } else {
            String old = current.get(k);
            if (old == null) {
                current.put(k, v);
                keyToConfigName.put(k, currentConfigName);
                LOG.debug("config file [" + currentConfigName + "] add [" + k + "] -> [" + v + "]");
            } else {
                if (old.equals(v))
                    LOG.warn("duplicated key ["
                            + k + "] defined in ["
                            + currentConfigName
                            + "], the two same values is [" + v + "]");
                else
                    throw new IllegalArgumentException(
                            "duplicated key [" + k
                                    + "] defined in ["
                                    + currentConfigName + "] first defined as ["
                                    + old + "] second defined as [" + v + "]");
            }
        }
    }

    public void addGlobal(String k, String v) {
        String old = globalProperties.get(k);
        if (old == null) {
            globalProperties.put(k, v);
            LOG.debug("add global property: [" + k + "] -> [" + v + "]");
        } else {
            if (old.equals(v))
                LOG.warn("duplicated global key ["
                        + k + "] the two same values is [" + v + "]");
            else
                throw new IllegalArgumentException(
                        "duplicated key [" + k
                                + "] first defined as ["
                                + old + "] second defined as [" + v + "]");
        }
    }

    /**
     * support multi property in one string
     * hahaha ${name sad} ooo ${second-hahaha} back-back
     *
     * do not support nested properties
     *
     * hahaha ${name sad${second-hahaha}} ooo  back-back -> goes wrong
     *
     *
     * @param name
     * @return
     */
    @Nonnull
    public String replaceIfPresent(@Nonnull String name) {
        int lq = name.indexOf("${", 0);
        if (lq == -1)
            return name;

        int rq;
        int lastL = 0;
        StringBuilder b = new StringBuilder(name.length() * 2);
        while (lq != -1) {
            rq = name.indexOf('}', lq);
            if (rq == -1)
                throw new IllegalArgumentException(
                        "configuration: unclosed property [" + name + "]");

            b.append(name.substring(lastL, lq));

            String k = name.substring(lq + 2, rq);
            String val = getVal(k.trim());
            if (val == null)
                throw new NoSuchElementException("cannot find property of key [" + k + "]");

            b.append(val);
            lq = name.indexOf("${", rq);
            lastL = rq + 1;
        }
        b.append(name.substring(lastL, name.length()));

        return b.toString();
    }

    @Nullable
    public String getVal(String k) {
        String conf = keyToConfigName.get(k);
        if (conf == null)
            return globalProperties.get(k);
        else
            return configs.get(conf).get(k);
    }

    public Entry getEntry(String k) {
        String confName = keyToConfigName.get(k);
        Map<String, String> conf = configs.get(confName);
        return conf == null ? new Entry(confName, k, globalProperties.get(k))
                : new Entry(confName, k, conf.get(k));
    }

    public static class Entry {
        final String configName;
        final String key;
        final String value;
        public Entry(String configName, String key, String value) {
            this.configName = configName;
            this.key = key;
            this.value = value;
        }
    }

    public void clear() {
        this.configs.clear();
        this.proxy = null;
        this.streamSupport = null;
        this.currentConfigName = null;
        this.globalProperties.clear();
        this.keyToConfigName.clear();
    }

    public InputStreamSupport getStreamSupport() {
        return streamSupport;
    }

    public void setDefaultStreamProvider(InputStreamProvider streamProvider) {
        proxy.setDefaultProvider(streamProvider);
    }
    public void setClassPathProvider(InputStreamProvider provider) {
        proxy.setClassPathProvider(provider);
    }

}
