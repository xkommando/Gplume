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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caibowen.gplume.cache.ICacheProvider;


/**
 * An abstract CachingFilter.
 * <p/>
 * This class should be sub-classed for each page to be cached.
 * <p/>
 * The filters must be declared in the web.xml deployment descriptor. Then a
 * mapping from a web resource, such as a JSP Page, FreeMarker page, Velocity
 * page, Servlet or static resouce needs to be defined. Finally, a succession of
 * mappings can be used to create a filter chain. See SRV.6 of the Servlet 2.3
 * specification for more details.
 * <p/>
 * Care should be taken not to define a filter chain such that the same
 * {@link CachingFilter} class is reentered. The {@link CachingFilter} uses the
 * {@link net.sf.ehcache.constructs.blocking.BlockingCache}. It blocks until the
 * thread which did a get which results in a null does a put. If reentry happens
 * a second get happens before the first put. The second get could wait
 * indefinitely. This situation is monitored and if it happens, an
 * IllegalStateException will be thrown.
 * <p/>
 * The following construct-params are supported:
 * <ol>
 * <li>cacheName - the id in ehcache.xml used by the filter.
 * <li>blockingTimeoutMillis - the time, in milliseconds, to wait for the filter
 * chain to return with a response on a cache miss. This is useful to fail fast
 * in the event of an infrastructure failure.
 * </ol>
 * 
 * @author Greg Luck
 * 
 * @author BowenCai
 */
public abstract class CachingFilter implements Filter {
    
    /**
     * The cache holding the web pages. Ensure that all threads for a given
     * cache id are using the same instance of this.
     */
    protected ICacheProvider cacheProvider;

    public void setCacheProvider(ICacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

	private final VisitLog visitLog = new VisitLog();

    /**
     * Build page info either using the cache or building the page directly.
     * <p/>
     * Some requests are for page fragments which should never be gzipped, or
     * for other pages which are not gzipped.
     */
    protected PageData getPageData(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain) throws Exception {
        // Look up the cached page
        final String key = calculateKey(request);
        PageData pageData = null;
        try {
        	
            checkNoReentry(request);
            
            pageData = (PageData) cacheProvider.getSync(key);
			if (pageData == null) {
				// Page is not cached - build the response, cache it, and
				// send to client
				pageData = buildPageData(request, response, chain);
System.out.println("building new cache[" + pageData.getDateCreated().getTime() + "]");
				if (pageData.isOk()) {
					cacheProvider.putAsync(key, pageData);
				}
			}
			
        } finally {
            // all done building page, reset the re-entrant flag
            visitLog.clear();
        }
        return pageData;
    }

    /**
     * Builds the PageInfo object by passing the request along the filter chain
     * 
     * @param request
     * @param response
     * @param chain
     * @return a Serializable value object for the page or page fragment
     * @throws ServletException 
     * @throws IOException 
     * @throws AlreadyGzippedException
     *             if an attempt is made to double gzip the body
     * @throws Exception
     */
    protected PageData buildPageData(final HttpServletRequest request,
    									final HttpServletResponse response, 
    									final FilterChain chain) throws IOException, ServletException {

        // Invoke the next entity in the chain
        final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        final GenericResponseWrapper wrapper = new GenericResponseWrapper(
                response, outstr);
        
        chain.doFilter(request, wrapper);
        
        wrapper.flush();

        int timeToLiveSeconds = cacheProvider.getExpiration();

        // Return the page info
        return new PageData(wrapper.getStatus(), wrapper.getContentType(),
                wrapper.getCookies(), outstr.toByteArray(), true,
                timeToLiveSeconds, wrapper.getAllHeaders());
    }
    /**
     * Writes the response from a PageInfo object.
     * <p/>
     * Headers are set last so that there is an opportunity to override
     * 
     * @param request
     * @param response
     * @param pageInfo
     * @throws IOException
     * @throws DataFormatException
     * @throws ResponseHeadersNotModifiableException
     * 
     */
    protected void writeResponse(final HttpServletRequest request,
            final HttpServletResponse response, final PageData pageInfo)
            throws IOException, DataFormatException {
    	
//        boolean requestAcceptsGzipEncoding = FilterUtil.acceptsEncoding(request, "gzip");

        setStatus(response, pageInfo);
        setContentType(response, pageInfo);
        setCookies(pageInfo, response);
        // do headers last so that users can override with their own header sets
        setHeaders(pageInfo, response);
        writeContent(request, response, pageInfo);
    }

    /**
     * Set the content type.
     * 
     * @param response
     * @param pageInfo
     */
    protected void setContentType(final HttpServletResponse response,
            final PageData pageInfo) {
        String contentType = pageInfo.getContentType();
        if (contentType != null && contentType.length() > 0) {
            response.setContentType(contentType);
        }
    }

    /**
     * Set the serializableCookies
     * 
     * @param pageInfo
     * @param response
     */
    protected void setCookies(final PageData pageInfo,
            final HttpServletResponse response) {

        final Collection<SerializableCookie> cookies = pageInfo.getSerializableCookies();
        for (Iterator<SerializableCookie> iterator = cookies.iterator(); iterator.hasNext();) {
            final Cookie cookie = ((SerializableCookie) iterator.next())
                    .toCookie();
            response.addCookie(cookie);
        }
    }

    /**
     * Status code
     * 
     * @param response
     * @param pageInfo
     */
    protected void setStatus(final HttpServletResponse response,
            final PageData pageInfo) {
        response.setStatus(pageInfo.getStatusCode());
    }

    /**
     * Set the headers in the response object, excluding the Gzip header
     * 
     * @param pageInfo
     * @param response
     */
    protected void setHeaders(final PageData pageInfo, final HttpServletResponse response) {

        final Collection<Header<? extends Serializable>> headers = pageInfo
                .getHeaders();

        // Track which headers have been set so all headers of the same id
        // after the first are added
        final TreeSet<String> setHeaders = new TreeSet<>(
                String.CASE_INSENSITIVE_ORDER);

        for (final Header<? extends Serializable> header : headers) {
        	
            final String name = header.getName();
            Serializable var = header.getValue();
            
            if (var instanceof String) {
            	
                if (setHeaders.contains(name)) {
System.out.println("addHeader " + name + "  " + (String)var);
                    response.addHeader(name, (String)var);
                } else {
                    setHeaders.add(name);
System.out.println("addHeader " + name + "  " + (String)var);
                    response.setHeader(name, (String)var);
                }
                
            } else if (var instanceof Long) {
            	
                if (setHeaders.contains(name)) {

System.out.println("addDateHeader " + name + "  " + (Long)var);
                    response.addDateHeader(name, (Long) var);
                } else {
                    setHeaders.add(name);

System.out.println("setDateHeader " + name + "  " + (Long)var);
                    response.setDateHeader(name, (Long)var);
                }
			} else if (var instanceof Integer) {
                if (setHeaders.contains(name)) {

System.out.println("addIntHeader " + name + "  " + (Integer)var);
                    response.addIntHeader(name, (Integer)var);
                } else {
                    setHeaders.add(name);

System.out.println("setIntHeader " + name + "  " + (Integer)var);
                    response.setIntHeader(name, (Integer)var);
                }
			} else {
                throw new IllegalArgumentException("No mapping for Header: "
                        + header);
            }
        }
    }

    /**
     * CachingFilter works off a key.
     * <p/>
     * The key should be unique. Factors to consider in generating a key are:
     * <ul>
     * <li>The various hostnames that a request could come through
     * <li>Whether additional parameters used for referral tracking e.g. google
     * should be excluded to maximise cache hits
     * <li>Additional parameters can be added to any page. The page will still
     * work but will miss the cache. Consider coding defensively around this
     * issue.
     * </ul>
     * <p/>
     * Implementers should differentiate between GET and HEAD requests otherwise
     * blank pages can result. See SimplePageCachingFilter for an example
     * implementation.
     * 
     * @param httpRequest
     * @return the key, generally the URL plus request parameters
     */
	protected String calculateKey(HttpServletRequest httpRequest) {
    	
        StringBuffer stringBuffer = new StringBuffer();
        
        stringBuffer.append(httpRequest.getMethod()).append(httpRequest.getRequestURI())
        				.append(httpRequest.getQueryString());

        return stringBuffer.toString();
    }
    /**
     * Writes the response content. This will be gzipped or non gzipped
     * depending on whether the User Agent accepts GZIP encoding.
     * <p/>
     * If the body is written gzipped a gzip header is added.
     * 
     * @param response
     * @param pageInfo
     * @throws IOException
     */
    protected void writeContent(final HttpServletRequest request,
            final HttpServletResponse response, final PageData pageInfo) throws IOException {
        byte[] body;

        boolean shouldBodyBeZero = PageCacheUtil.shouldBodyBeZero(request,
                pageInfo.getStatusCode());
        if (shouldBodyBeZero) {
            body = new byte[0];
        } else if (PageCacheUtil.acceptsEncoding(request, "gzip")) {
            body = pageInfo.getGzippedBody();
            if (PageCacheUtil.shouldGzippedBodyBeZero(body, request)) {
                body = new byte[0];
            } else {
                PageCacheUtil.addGzipHeader(response);
            }

        } else {
            body = pageInfo.getUngzippedBody();
        }

        response.setContentLength(body.length);
        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        out.write(body);
        out.flush();
    }

    /**
     * Check that this caching filter is not being reentered by the same
     * recursively. Recursive calls will block indefinitely because the first
     * request has not yet unblocked the cache.
     * <p/>
     * This condition usually indicates an error in filter chaining or
     * RequestDispatcher dispatching.
     * 
     * @param httpRequest
     * @throws FilterNonReentrantException
     *             if reentry is detected
     */
    protected void checkNoReentry(final HttpServletRequest httpRequest) {
    	
        
        if (visitLog.hasVisited()) {
        	
            String filterName = getClass().getName();
            
            throw new RuntimeException( "FilterNonReentrantException  " +
                    "The request thread is attempting to reenter" + " filter "
                            + filterName + ". URL: "
                            + httpRequest.getRequestURL());
        } else {
            // mark this thread as already visited
            visitLog.markAsVisited();
            
//System.out.println("Thread ["+ Thread.currentThread().getName()
//+"]  has been marked as visited");
        }
    }
	
    /**
     * threadlocal class to check for reentry
     * 
     * @author hhuynh
     * 
     */
    private static class VisitLog extends ThreadLocal<Boolean> {
    	
		public VisitLog() {}

		@Override
        protected Boolean initialValue() {
            return false;
        }

        public boolean hasVisited() {
            return get();
        }

        public void markAsVisited() {
            set(true);
        }

        public void clear() {
            super.remove();
        }
    }
}
