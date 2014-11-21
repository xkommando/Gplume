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

package com.caibowen.gplume.misc.test.stream;

import com.caibowen.gplume.context.InputStreamProvider;
import com.caibowen.gplume.context.InputStreamSupport;
import com.caibowen.gplume.web.RequestContext;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * <pre>
 * usage:
 * add enctype  &lt;form enctype="multipart/form-data" &gt;
 *   Upload.open(requestContext).withPath("icon", new InputStreamCallback() ...
 *
 *  Upload p = Upload.open(request);
 *  p.withPath("icon 1", new InputStreamCallback()
 *  p.withPath("icon 2", new InputStreamCallback()
 *</pre>
 *  @author bowen.cbw
 * @since 8/26/2014.
 */
public class Upload extends InputStreamSupport {

    public static Upload open(@Nonnull RequestContext context) {
        return new Upload(context.request);
    }

    public final ServletFileUpload upload;
    public final HttpServletRequest request;

    private Upload(final HttpServletRequest request) {
        this.upload = new ServletFileUpload();
        this.request = request;

        super.setStreamProvider(new InputStreamProvider() {

            @Override
            public InputStream getStream(String path) throws IOException {
                try {
                    FileItemIterator iter = upload.getItemIterator(request);
                    while (iter.hasNext()) {
                        FileItemStream stm = iter.next();
                        if (stm.getFieldName().equals(path)) {
                            return stm.openStream();
                        }
                    }
                } catch (Exception e) {
                    throw new IOException(e);
                }
                throw new IOException(path + " does not exists");
            }

            @Override
            public String getRealPath(String p) {
                throw new UnsupportedOperationException();
            }
        });

    }

}
