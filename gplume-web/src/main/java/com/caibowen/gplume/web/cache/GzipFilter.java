/*******************************************************************************
 * Copyright 2014 Bowen Cai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 *  Copyright 2003-2009 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.caibowen.gplume.web.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Provides GZIP compression of responses.
 * <p/>
 * See the filter-mappings.xml entry for the gzip filter for the URL patterns which will be gzipped. At present this
 * includes .jsp, .js and .css.
 * <p/>
 * 
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 * @author <a href="mailto:amurdoch@thoughtworks.com">Adam Murdoch</a>
 * 
 * @author BowenCai
 */
public class GzipFilter implements Filter {
	
    private boolean setVaryHeader;

    private static final Logger LOG = LoggerFactory.getLogger(Package.class);
    /**
     * Performs initialisation.
     * 
     * @param filterConfig
     */

    @Override
	public final void init(final FilterConfig filterConfig) throws ServletException {
    	
    }

    /**
     * Performs the filtering for a request.
     * @throws ServletException 
     * @throws IOException 
     */
    @Override
	public void doFilter(final ServletRequest req,
							final ServletResponse resp, 
							final FilterChain chain) throws IOException, ServletException {
    	
    	HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse) resp;
    	
		String ecd = request.getHeader("Accept-Encoding");
		if (ecd != null && ecd.indexOf("gzip") != -1) {
			LOG.debug("OK [" + ecd + "]");
		} else {
            LOG.debug("cannot be gzipped [" + ecd + "]");
		}
		
        if (!PageCacheUtil.isIncluded(request) && PageCacheUtil.acceptsEncoding(request, "gzip") 
        		&& !response.isCommitted()) {
            // Client accepts zipped content

            LOG.debug(". Writing with gzip compression req ["  + request.getRequestURI() + "]");
            // Create a gzip stream
            final ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            final GZIPOutputStream gzout = new GZIPOutputStream(compressed);

            // Handle the request
            final GenericResponseWrapper wrapper = new GenericResponseWrapper(response, gzout);
            wrapper.setDisableFlushBuffer(true);
            chain.doFilter(request, wrapper);
            wrapper.flush();

            gzout.close();

            // double check one more time before writing out
            // repsonse might have been committed due to error
            if (response.isCommitted()) {
                return;
            }
            
            // return on these special cases when content is empty or unchanged
			switch (wrapper.getStatus()) {
			case HttpServletResponse.SC_NO_CONTENT:
			case HttpServletResponse.SC_RESET_CONTENT:
			case HttpServletResponse.SC_NOT_MODIFIED:
				return;
			default:
			}

            // Saneness checks
            byte[] compressedBytes = compressed.toByteArray();
            boolean shouldGzippedBodyBeZero = PageCacheUtil.shouldGzippedBodyBeZero(compressedBytes, request);
            boolean shouldBodyBeZero = PageCacheUtil.shouldBodyBeZero(request, wrapper.getStatus());
            if (shouldGzippedBodyBeZero || shouldBodyBeZero) {
                // No reason to add GZIP headers or write body if no content was written or status code specifies no
                // content
                response.setContentLength(0);
                return;
            }

            // Write the zipped body
            PageCacheUtil.addGzipHeader(response);

            // Only write out header Vary as needed
            if (setVaryHeader) {
                PageCacheUtil.addVaryAcceptEncoding(wrapper);
            }

            response.setContentLength(compressedBytes.length);

            response.getOutputStream().write(compressedBytes);

        } else {
            // Client does not accept zipped content - don't bother zipping
            LOG.debug(". Writing without gzip compression because the request does not accept gzip.req [" + request.getRequestURI() + "]");
            chain.doFilter(request, response);
        }
    }

	@Override
	public void destroy() {}


}
