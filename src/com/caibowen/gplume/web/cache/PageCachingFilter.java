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

/*
 * Based on a contribution from Craig Andrews which has been released also under the Apache 2 license at
 * http://candrews.integralblue.com/2009/02/http-caching-header-aware-servlet-filter/. Copyright notice follows.
 *
 * Copyright 2009 Craig Andrews
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

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caibowen.gplume.cache.ICacheProvider;
import com.caibowen.gplume.core.context.AppContext;



/**
 * This Filter extends {@link SimplePageCachingFilter}, adding support for
 * the HTTP cache headers: ETag, Last-Modified and Expires.
 * <p>
 * Because browsers and other HTTP clients have the expiry information returned in the response headers,
 * they do not even need to request the page again. Even once the local browser copy has expired, the browser
 * will do a conditional GET.
 * <p>
 * So why would you ever want to use SimplePageCachingFilter, which does not set these headers? Because in some caching
 * scenarios you may wish to remove a page before its natural expiry. Consider a scenario where a web page shows dynamic
 * data. Under Ehcache the Element can be removed at any time. However if a browser is holding expiry information, those
 * browsers will have to wait until the expiry time before getting updated. The caching in this scenario is more about
 * defraying server load rather than minimising browser calls.
 * <p>
 *
 * @author Craig Andrews
 * @author Greg Luck
 * 
 * @author BowenCai
 */
public class PageCachingFilter extends CachingFilter {
	
    private SimpleDateFormat httpDateFormatter = null;
	

    /**
     * Builds the PageInfo object by passing the request along the filter chain
     * <p>
     * The following headers are set:
     * <ul>
     * <li>Last-Modified
     * <li>Expires
     * <li>Cache-Control
     * <li>ETag
     * </ul>
     * Any of these headers aleady set in the response are ignored, and new ones generated. To control
     * your own caching headers, use {@link SimplePageCachingFilter}.
     *
     *
     * @param request
     * @param response
     * @param chain
     * @return a Serializable value object for the page or page fragment
     * @throws ServletException 
     * @throws IOException 
     * @throws AlreadyGzippedException if an attempt is made to double gzip the body
     * @throws Exception
     *
     */
    @Override
    protected PageData buildPageData(HttpServletRequest request, 
    								HttpServletResponse response, 
    								FilterChain chain) throws IOException, ServletException{
    	
        PageData pageInfo = super.buildPageData(request, response, chain);

        final List<Header<? extends Serializable>> headers = pageInfo.getHeaders();
        
        //Remove any conflicting headers
        for (final Iterator<Header<? extends Serializable>> headerItr = headers.iterator();
        		headerItr.hasNext();) {
        	
            final Header<? extends Serializable> header = headerItr.next();
            
            final String name = header.getName();
            if ("Last-Modified".equalsIgnoreCase(name) || 
                    "Expires".equalsIgnoreCase(name) || 
                    "Cache-Control".equalsIgnoreCase(name) || 
                    "ETag".equalsIgnoreCase(name)) {
                headerItr.remove();
            }
        }
        
        //add expires and last-modified headers
        
        //trim the milliseconds off the value since the header is only accurate down to the second
        long lastModified = pageInfo.getDateCreated().getTime();
        lastModified = TimeUnit.MILLISECONDS.toSeconds(lastModified);
        lastModified = TimeUnit.SECONDS.toMillis(lastModified);
        

        long ttlMilliseconds = cacheProvider.getExpiration() * 1000L;
        
        headers.add(new Header<Long>("Last-Modified", lastModified));
        headers.add(new Header<Long>("Expires", System.currentTimeMillis() + ttlMilliseconds));
        headers.add(new Header<String>("Cache-Control", "max-age=" + ttlMilliseconds / 1000));
        headers.add(new Header<String>("ETag", generateEtag(ttlMilliseconds)));
//System.err.println("null ??? " + pageInfo);
        return pageInfo;
    }


    /**
     * ETags are required to have double quotes around the value, unlike any other header.
     * <p/>
     * The ehcache eTag is effectively the Expires time, but accurate to milliseconds, i.e.
     * no conversion to the nearest second is done as is done for the Expires tag. It therefore
     * is the most precise indicator of whether the client cached version is the same as the server
     * version.
     * <p/>
     * MD5 is not used to calculate ETag, as it is in some implementations, because it does not
     * add any extra value in this situation, and it has a higher cost.
     *
     * @see "http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.3"
     */
    private static String generateEtag(long ttlMilliseconds) {
    	
        Long eTagRaw = System.currentTimeMillis() + ttlMilliseconds;
        return new StringBuilder(21).append('\"').append(eTagRaw).append('\'').toString();
    }
    
    @Override
	public void doFilter(final ServletRequest req,
							final ServletResponse resp, 
							final FilterChain chain) throws IOException{

    	HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
        if (response.isCommitted()) {
            throw new RuntimeException(
                    "Response already committed before doing buildPage. req [" + request.getRequestURL() + "]");
        }
        
		try {
			PageData page = getPageData(request, response, chain);
			if (page.isOk()) {
//				if (response.isCommitted()) {
//					throw new RuntimeException(
//							"Response already committed after doing buildPage"
//									+ " but before writing response from PageInfo.");
//				}
				writeResponse(request, response, page);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}


    }
    /**
     * Writes the response from a PageInfo object.
     *
     * This method actually performs the conditional GET and returns 304
     * if not modified, short-circuiting the normal writeResponse.
     * <p/>
     * Indeed, if the short cicruit does not occur it calls the super method.
     */
	@Override
	protected void writeResponse(HttpServletRequest request,
									HttpServletResponse response,
									PageData pageInfo) throws IOException, DataFormatException {

		final List<Header<? extends Serializable>> headers = pageInfo.getHeaders();

		for (final Header<? extends Serializable> header : headers) {
			
			if ("ETag".equals(header.getName())) {
				
				String requestIfNoneMatch = request.getHeader("If-None-Match");
				
				if (header.getValue().equals(requestIfNoneMatch)) {
					response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
					// use the same date we sent when we created the ETag the
					// first time through
					// response.setHeader("Last-Modified",
					// request.getHeader("If-Modified-Since"));
					return;
				}
				break;
			} //if ("ETag".equals(header.getName()))
			
			if ("Last-Modified".equals(header.getName())) {
				
				try {
//System.out.println("If-Modified-Since["+request.getHeader("If-Modified-Since")+"]");
					String ms = request.getHeader("If-Modified-Since");

					if (ms != null) {

						long requestIfModifiedSince = 
								httpDateFormatter.parse(request.getHeader("If-Modified-Since")).getTime();
						
						final Date requestDate = new Date(requestIfModifiedSince);
						
						final Date pageInfoDate;
						Serializable var = header.getValue();
						if (var instanceof String) {
							pageInfoDate = httpDateFormatter.parse((String)var);
						} else if (var instanceof Long || var instanceof Integer) {
							pageInfoDate = new Date((Long)var);
						} else {
							throw new IllegalArgumentException("Header "
							+ header + " is not supported");
						}			

						if (!requestDate.before(pageInfoDate)) {
							response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
							response.setHeader("Last-Modified", request.getHeader("If-Modified-Since"));
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} // ("Last-Modified".equals(header.getName()))
			
		} // for headers

		super.writeResponse(request, response, pageInfo);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		ICacheProvider cacheProvider = AppContext.beanAssembler.getBean("pageCache");
		setCacheProvider(cacheProvider);
		
		httpDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		httpDateFormatter.setTimeZone(AppContext.defaults.timeZone);
	}


}


