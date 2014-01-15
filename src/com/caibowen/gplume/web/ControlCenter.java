package com.caibowen.gplume.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.util.Str;
import com.caibowen.gplume.web.note.HTTPMethod;
import com.caibowen.gplume.web.note.Handler;


/**
 * 
 * Instantiate all controller,
 * 
 * set their properties
 * 
 * scan for handle function, 
 * and make actors out of handle function and the controller instance.
 * 
 * finally, put actors into different request handlers
 * 
 * @author BowenCai
 *
 */
public class ControlCenter {

//-----------------------------------------------------------------------------
//					handler
//-----------------------------------------------------------------------------

	private static final Logger LOG = Logger.getLogger(ControlCenter.class.getName());
	
	private RequestHandler getHandler = new RequestHandler();
	private RequestHandler postHandler = new RequestHandler();
	
	@Inject 
	IErrorHandler  errorHandler = new DefaultErrorHandler();
	/**
	 * you can remove error handler by calling removeController
	 *  and set a new one dynamically
	 * @param errorHandler
	 */
	public void setErrorHandler(IErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	@Inject
	private List<String> controllerList;
	public void setControllerList(List<String> controllerList) {
		this.controllerList = controllerList;
	}
	
	@Inject
	private Injector injector;
	public void setInjector(Injector injector) {
		this.injector = injector;
	}


	ServletContext servletContext = null;
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void process(HttpServletRequest request, 
							HttpServletResponse response) {
		

		Request req = new Request(request, response);
		
//System.out.println("===" + request.getParameter("value"));
//		String uri = RequestUtil.getUri(request);
//        String _uri = RequestUtil.getUri(request);
//        
//        for (int lastIndex = _uri.lastIndexOf('/'); lastIndex > (-1); lastIndex = _uri.lastIndexOf('/', lastIndex - 1)) {
//        	System.out.println(">>>[" + _uri.substring(0, lastIndex)+"]");
//        }
// 
////		String uri = request.getRequestURI();
////	    public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {
////	        String uri = RequestUtils.getUri(request);
////	        for (int lastIndex = uri.lastIndexOf('/'); lastIndex > (-1); lastIndex = uri.lastIndexOf('/', lastIndex - 1)) {
////	            ActionMapper actionMapper = actionMappers.get(uri.substring(0, lastIndex));
////	            if (actionMapper != null) {
//System.out.println("uri[" + uri + "]");
		String actionName = null;
//		int idx = uri.lastIndexOf('/');
//		if (idx > -1) {
//			actionName = uri.substring(0, idx);			
//
//System.out.println(" > -1 action name [" + actionName + "]" + actionName.length() + " idx " + idx);
//
//		} else {
//			ErrorHandlers.X404(req);
//			return;
//		}
		actionName = request.getRequestURI();
		doProcess(actionName, req);
	}
	
	
	void doProcess(String actionName, Request request) {
		
		HTTPMethod httpMethod = request.getMethod();

		boolean handled= false;
		boolean throwed = false;
		try {
			switch (httpMethod) {
			
			case GET:
				handled = getHandler.handle(actionName, request);
				break;
			case POST:
				handled = postHandler.handle(actionName, request);
				break;
			default:
				break;
			}
			
		} catch (UndeclaredThrowableException udefe) {
			
			LOG.log(Level.SEVERE, 
					"invokation error " + udefe.getMessage(), 
					udefe.getUndeclaredThrowable());
			throwed = true;
			
		} catch (IOException ioex) {
			
			LOG.log(Level.SEVERE, "I/O Error", ioex);
			throwed = true;
			
		} catch (ServletException servex) {

			LOG.log(Level.SEVERE, "Servlet Error", servex);
			throwed = true;
			
		}  catch(Exception e) {
			
			LOG.log(Level.SEVERE,
					"Other exception: " + e.getClass().getName() 
					+ "\n Message" + e.getMessage() 
					+ "\n Cause: " + e.getCause(),
					e);
			throwed = true;
			
		}catch (Throwable thr) {
			
			LOG.log(Level.SEVERE,
					"Throwable : " + thr.getMessage() + "\n Cause: " + thr.getCause(),
					thr);
			throwed = true;
		}
		
		if (throwed) {
			errorHandler.http500(request);
		} else if ( !handled) {
			errorHandler.http404(request);
		}
	}
	
	
	/**
	 * called at application start, after which no such exception would be thrown
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws Throwable
	 */
	public void prepare() {
			
		for (String clazzName : controllerList) {

			Class<?> clazz = null;
			Object ctrlObj = null;
			try {
				clazz = Class.forName(clazzName.trim());
				ctrlObj = clazz.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(
						"error instantiate controller class\n Cause : " + e.getCause()
						, e);
			}
			
			
			try {
				addController(ctrlObj);
			} catch (Exception e) {
				throw new RuntimeException(
						"error inject/scan controller object\nCause : " + e.getCause()
						, e);
			}
		}

	}
	
	/**
	 * add controller object dynamically
	 * @param ctrlObj
	 * @throws Exception
	 */
	public void addController(Object ctrlObj) throws Exception {

		injector.inject(ctrlObj);
		
		scanForHandler(ctrlObj);
	}
	
	public boolean removeController(Object ctrlObj) {
		if (ctrlObj.equals(errorHandler)) {
			errorHandler = new DefaultErrorHandler();
			return true;
		} else {
			return getHandler.remove(ctrlObj) || postHandler.remove(ctrlObj);
		}
	}
	//-----------------------------------------------------------------------------
//	  				java.lang.invoke
	//-----------------------------------------------------------------------------

	private static final Lookup LOOKUP = MethodHandles.publicLookup();

	private static final MethodType ACION_TYPE = MethodType.methodType(
														void.class, Request.class);
	/**
	 * 
	 * @param obj
	 * @throws Exception exception is thrown when looking for handlers
	 */
	private void scanForHandler(Object obj) throws Exception {

		Class<?> clazz = obj.getClass();
		// public only
		Method[] methods = clazz.getMethods();

		try {// exception impossible 
			for (Method method : methods) {
				if (method.isAnnotationPresent(Handler.class)) {
					String mName = method.getName();
					
					MethodHandle actionhandle 
							= LOOKUP.findVirtual(clazz, mName, ACION_TYPE);

					Actor actor = new Actor(obj, actionhandle);
					
					dispacht(method.getAnnotation(Handler.class), actor);
				}
			}
		} catch (NoSuchMethodException e) {
			// impossible, method name is from method.getName();
			e.printStackTrace();// for debugging
		}
	}
	
	private void dispacht(Handler anno, Actor actor) throws Exception {

		String[] uris = anno.uri();
		
		HTTPMethod ty = anno.httpMethod();
		
		// check null
		if (uris == null 
				|| (uris != null) && uris.length == 0) {
			
			throw new NullPointerException("uri is empty"
				+" the controller object is [" + actor.object.getClass().getName() + "]");
		}
		
		// check URI
		for (String uri : uris) {
			
			int len = uri.length();
			
			if (uri.charAt(len - 1) != '*') {
				if (!Str.Patterns.PART_URI.matcher(uri).matches()) {
					throw new IllegalArgumentException("illegal url[" + uri + "]");
				}
			} else {
				if (!Str.Patterns.PART_URI.matcher(
						uri.substring(0, len - 1)).matches()) {
					throw new IllegalArgumentException("illegal url[" + uri + "]");
				}
			}
			
			// do dispatch
			switch (ty) {

			case GET:
				getHandler.add(uri, actor);
				break;

			case POST:
				postHandler.add(uri, actor);
				break;

			default:
				break;
			}
		}

	}
	
// -----------------------------------------------------------------------------
// HTTP
// private static final String HEADER_IFMODSINCE = "If-Modified-Since";
// private static final String HEADER_LASTMOD = "Last-Modified";

	/**
	 * Request is a wrapper class of requst and response
	 * Request made a inner class to get reference to controlCenter for free
	 * 
	 * @author BowenCai
	 *
	 */
	public class Request {
		
		public final HttpServletRequest		in;
		public final HttpServletResponse	out;
		
		public final HTTPMethod method;
		/**
		 * strictly speaking, only control center can create it 
		 * 
		 * @param in
		 * @param out
		 */
		Request(HttpServletRequest in, HttpServletResponse out) {
			
			this.in = in;
			this.out = out;
			
			String mStr = in.getMethod();
			HTTPMethod _m = HTTPMethod.GET;
			switch (mStr) {
			case METHOD_GET:
				_m = HTTPMethod.GET;
				break;
			case METHOD_POST:
				_m = HTTPMethod.POST;
				break;
			case METHOD_PUT:
				_m = HTTPMethod.PUT;
				break;
			case METHOD_DELETE:
				_m = HTTPMethod.DELETE;
				break;
			case METHOD_HEAD:
				_m = HTTPMethod.HEAD;
				break;
			case METHOD_TRACE:
				_m = HTTPMethod.TRACE;
				break;
			case METHOD_OPTIONS:
				_m = HTTPMethod.OPTIONS;
				break;
// default impossible
//			default:
//				throw new NoSuchMethodError(
//						"no match for httpServlet request method[" + mStr + "]");
			}
			method = _m;
		}
		

		public void forward(String url) throws ServletException, IOException {
			RequestDispatcher dispatcher = in.getRequestDispatcher(url);
			dispatcher.forward(in, out);
		}
		
		public void redirect(String url) throws IOException {
			out.sendRedirect(url);
		}
		
		public void passOn(String actionName) {
			ControlCenter.this.doProcess(actionName, this);
		}
//-----------------------------------------------
		public void setCookie(Cookie ck) {
			out.addCookie(ck);
		}
		
		@CheckForNull
		@Nullable
		public Cookie getCookie(String name) {
			Cookie[] cookies = in.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(name)) {
						return cookie;
					}
				}
			}
			return null;
		}
		
		public PrintWriter getOut() throws IOException {
			return out.getWriter();
		}
		
		public HTTPMethod getMethod() {
			return method;
		}
		
		public HttpSession getSession() {
			return in.getSession();
		}
		public HttpSession getSession(boolean boo) {
			return in.getSession(boo);
		}
		
		public ServletContext getServletContext() {
			return ControlCenter.this.servletContext;
		}
		
		public IErrorHandler getErrorHandler() {
			return ControlCenter.this.errorHandler;
		}
		
//-----------------------------------
		
		public void put(String name, Object value) {
			in.setAttribute(name, value);
		}
		
		public void remove(String name) {
			in.removeAttribute(name);
		}
		
//-----------------------------------

		@Nullable
		public Integer getIntParam(String name) {

			String s = in.getParameter(name);
			if (s != null) {
				return Integer.valueOf(name);
			} else {
				return null;
			}
		}

		public int getIntParam(String name, int defaultVar) {

			String s = in.getParameter(name);
			if (s != null) {
				return Integer.valueOf(name);
			} else {
				return defaultVar;
			}
		}

		@Nullable
		public int[] getIntsParam(String name) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				int[] vars = new int[s.length];
				for (int i = 0; i < s.length; i++) {
					vars[i] = Integer.valueOf(s[i]);
				}
				return vars;
			} else {
				return null;
			}
		}

		public int[] getIntsParam(String name, int[] defaultVar) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				return getIntsParam(name);
			} else {
				return defaultVar;
			}
		}

// ------------------------------------
		@Nullable
		public Long getLongParam(String name) {

			String s = in.getParameter(name);
			if (s != null) {
				return Long.valueOf(name);
			} else {
				return null;
			}
		}

		public Long getLongParam(String name, long defaultVar) {

			String s = in.getParameter(name);
			if (s != null) {
				return Long.valueOf(name);
			} else {
				return defaultVar;
			}
		}

		@Nullable
		public long[] getLongsParam(String name) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				long[] vars = new long[s.length];
				for (int i = 0; i < s.length; i++) {
					vars[i] = Long.valueOf(s[i]);
				}
				return vars;
			} else {
				return null;
			}
		}

		public long[] getLongsParam(String name, long[] defaultVar) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				return getLongsParam(name);
			} else {
				return defaultVar;
			}
		}
// -------------------------------------

		@Nullable
		public Float getFloatParam(String name) {

			String s = in.getParameter(name);
			if (s != null) {
				return Float.valueOf(name);
			} else {
				return null;
			}
		}

		public float getFloatParam(String name, float defaultVar) {

			String s = in.getParameter(name);
			if (s != null) {
				return Float.valueOf(name);
			} else {
				return defaultVar;
			}
		}

		@Nullable
		public float[] getFloatsParam(String name) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				float[] vars = new float[s.length];
				for (int i = 0; i < s.length; i++) {
					vars[i] = Float.valueOf(s[i]);
				}
				return vars;
			} else {
				return null;
			}
		}

		public float[] getFloatsParam(String name, float[] defaultVar) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				return getFloatsParam(name);
			} else {
				return defaultVar;
			}
		}

// -------------------------------------
		@Nullable
		public Double getDoubleParam(String name) {

			String s = in.getParameter(name);
			if (s != null) {
				return Double.valueOf(name);
			} else {
				return null;
			}
		}

		public double getDoubleParam(String name, double defaultVar) {

			String s = in.getParameter(name);
			if (s != null) {
				return Double.valueOf(name);
			} else {
				return defaultVar;
			}
		}

		@Nullable
		public double[] getDoublesParam(String name) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				double[] vars = new double[s.length];
				for (int i = 0; i < s.length; i++) {
					vars[i] = Double.valueOf(s[i]);
				}
				return vars;
			} else {
				return null;
			}
		}

		public double[] getDoublesParam(String name, double[] defaultVar) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				return getDoublesParam(name);
			} else {
				return defaultVar;
			}
		}

// ---------------------------------------
		@Nullable
		public Boolean getBoolParam(String name) {

			String s = in.getParameter(name);
			if (s != null) {
				return (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on")
						|| s.equalsIgnoreCase("yes") || s.equals("1"));
			} else {
				return null;
			}
		}

		public boolean getBoolParam(String name, boolean defaultVar) {

			String s = in.getParameter(name);
			if (s != null) {
				return (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on")
						|| s.equalsIgnoreCase("yes") || s.equals("1"));
			} else {
				return defaultVar;
			}
		}

		@Nullable
		public boolean[] getBoolsParam(String name) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				boolean[] vars = new boolean[s.length];
				for (int i = 0; i < s.length; i++) {
					vars[i] = getBoolParam(s[i]);
				}
				return vars;
			} else {
				return null;
			}
		}

		public boolean[] getBoolsParam(String name, boolean[] defaultVar) {

			String[] s = in.getParameterValues(name);
			if (s != null) {
				return getBoolsParam(name);
			} else {
				return defaultVar;
			}
		}
		
		@Nullable
		public String getParamStr(String name) {
			return in.getParameter(name);
		}
		
		public String getParamStr(String name, String def) {

			String ret = in.getParameter(name);
			return ret == null ? def : ret;
		}
		
		@Nullable
		public String[] getParamStrArray(String name) {
			return in.getParameterValues(name);
		}
		
		public String[] getParamStrArray(String name, String[] def) {
			
			String[] ret = in.getParameterValues(name);
			return ret == null ? ret : def;
		}

		
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
		private static final String METHOD_DELETE = "DELETE";
		private static final String METHOD_HEAD = "HEAD";
		private static final String METHOD_GET = "GET";
		private static final String METHOD_OPTIONS = "OPTIONS";
		private static final String METHOD_POST = "POST";
		private static final String METHOD_PUT = "PUT";
		private static final String METHOD_TRACE = "TRACE";
	}
	
}
//
//@Inject
//public void setErrorHandler(String clazzName) {
//
//		try {
//			Object _handler =  Class.forName(clazzName).newInstance();
//			if (_handler instanceof IErrorHandler) {
//				this.errorHandler = (IErrorHandler)_handler;
//			} else {
//				throw new IllegalArgumentException(
//						"class [" + clazzName + "] is not IErrorHandler"
//						+"\nrather it is [" + _handler.getClass().getName() + "]");
//			}
//		} catch (InstantiationException | IllegalAccessException
//				| ClassNotFoundException e) {
//			e.printStackTrace();
//			
//			throw new RuntimeException(
//					"cannot instantiate error handler class[" + clazzName + "]"
//					+ "\nError:" + e.getMessage(),
//					e);
//		}
//}





