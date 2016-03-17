package com.caibowen.gplume.context;

import com.caibowen.gplume.resource.ClassLoaderInputStreamProvider;

/**
 * Created by bowen on 3/15/2016.
 */
public class SimpleBoot {

    public static void main(String[] args) {
        ContextBooter booter = new ContextBooter();
        ClassLoader cloader =SimpleBoot.class.getClassLoader();
        booter.setClassLoader(cloader);
        booter.setStreamProvider(new ClassLoaderInputStreamProvider(cloader));
        booter.setManifestPath("assemble.xml");
        booter.boot();
    }
}
