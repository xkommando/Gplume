package com.caibowen.gplume.context;

import com.caibowen.gplume.resource.ClassLoaderInputStreamProvider;
import com.caibowen.gplume.resource.FileInputStreamProvider;
import com.caibowen.gplume.resource.InputStreamProviderProxy;

import java.io.File;

/**
 * Created by bowen on 3/15/2016.
 */
public class SimpleBoot {

    public static void main(String[] args) {
        booter.setClassLoader(cloader);
        booter.setStreamProvider(new ClassLoaderInputStreamProvider(cloader));
        booter.setManifestPath("assemble.xml");

        if (tryNames(booter, firstName) || tryNames(booter, secondName))
            booter.boot();
        else
            throw new IllegalArgumentException(
                    "cannot find [" + firstName + "] or [" + secondName + "] in classpath or root directory");
    }

    private static ContextBooter booter = new ContextBooter();
    public static String firstName = "assemble.xml";
    public static String secondName = "assemble-test.xml";

    private static ClassLoader cloader = SimpleBoot.class.getClassLoader();
    private static boolean tryNames(ContextBooter b, String name) {
        File f = new File(name);
        if (f.isFile()) {
            b.setManifestPath(name);
            b.setStreamProvider(new InputStreamProviderProxy(new FileInputStreamProvider()));
            ContextBooter.LOG.info("Using configuration file[" + f.getAbsolutePath() + "]");
            return true;
        } else if (cloader.getResource(name) != null) {
            b.setManifestPath(name);
            b.setStreamProvider(new InputStreamProviderProxy(new ClassLoaderInputStreamProvider(cloader)));
            ContextBooter.LOG.info("Using configuration file[" + name + "] found in class path");
            return true;
        } else return false;
    }
}
