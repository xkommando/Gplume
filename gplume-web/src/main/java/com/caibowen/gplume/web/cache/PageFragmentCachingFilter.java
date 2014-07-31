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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




/**
 * A Template for a page caching filter that is designed for "included" pages, eg: jsp:includes.  This filter
 * differs from the {@link CachingFilter} in that it is not writing an entire response to the output stream.
 * <p/>
 * This class should be sub-classed for each included page to be cached.
 * <p/>
 * Filter Mappings need to be set up for a cache to have effect.
 *
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 * 
 * @author BowenCai
 */
public class PageFragmentCachingFilter extends CachingFilter {

    /**
     * Performs the filtering for a request.
     * @throws IOException 
     */
    @Override
	public void doFilter(final ServletRequest req,
							final ServletResponse resp, 
							final FilterChain chain){
    	
    	HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse) resp;

        PageData pageInfo;
		try {
			pageInfo = getPageData(request, response, chain);
	        writeResponse(response, pageInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * {@inheritDoc}
     *
     * @param request  {@inheritDoc}
     * @param response {@inheritDoc}
     * @param chain    {@inheritDoc}
     * @return {@inheritDoc}
     * @throws ServletException 
     * @throws IOException 
     * @throws AlreadyGzippedException {@inheritDoc}
     */
    @Override
	protected PageData buildPageData(final HttpServletRequest request, 
									final HttpServletResponse response,
									final FilterChain chain) throws IOException, ServletException {

        // Invoke the next entity in the chain
        final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        final GenericResponseWrapper wrapper = new GenericResponseWrapper(response, outstr);
        
        chain.doFilter(request, wrapper);
        
        wrapper.flush();

        int timeToLiveSeconds = cacheProvider.getExpiration();

        // Return the page info
        return new PageData(wrapper.getStatus(), wrapper.getContentType(), 
                wrapper.getCookies(),
                outstr.toByteArray(), false, timeToLiveSeconds, wrapper.getAllHeaders());
    }


    /**
     * Assembles a response from a cached page include.
     * These responses are never gzipped
     * The content length should not be set in the response, because it is a fragment of a page.
     * Don't write any headers at all.
     */
    protected void writeResponse(final HttpServletResponse response, final PageData pageInfo) throws IOException {
        // Write the page
        final byte[] cachedPage = pageInfo.getUngzippedBody();
        //needed to support multilingual
        final String page = new String(cachedPage, response.getCharacterEncoding());
        response.getWriter().write(page);
    }

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}


}
