Gplume
======

Light-weight multiparadigmatic Java web framework.

Features:
 - easy to learn and use
 - mixed Spring styled and Struts2 styled request handling
 - high performance.
 - cross cloud.

----------------------------
Quick Start:
#####Part Zero: maven dependency
```XML
<dependency>
	<groupId>com.caibowen</groupId>
	<artifactId>gplume-webex</artifactId><!--extensions for web develop-->
	<version>1.5</version>
</dependency>
```
#####Part One: Configurate the web.xml:

```XML
	<context-param>
        <param-name>manifest</param-name>
        <param-value>classpath:app-manifest.xml</param-value><!-- in your class path -->
    </context-param>
	<listener>
		<listener-class>com.caibowen.gplume.web.WebAppBooter</listener-class>
	</listener>
		<filter>
		<filter-name>dispatcher</filter-name>
		<filter-class>com.caibowen.gplume.web.GFilter</filter-class>
	</filter>
	<filter-mapping>
		<filter-name>dispatcher</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```
#####Part Two: The IoC Container.
Spring-like configuration:
```XML
<import>some_other_config.xml</import>
<import>so_many_configs.xml</import>

<properties import="kv_pairs.properties">
	<gplumeVersion>1.0.0.nightly</gplumeVersion>
	<webErrorHandler>com.caibowen.web.misc.ErrorHandler</webErrorHandler>
</properties>

<properties import="kv_pairs_of_xml_format.xml" />

<bean class="com.caibowen.gplume.web.WebConfig" >
	<property name="preProcessor" ref="headPrePrcessor"/>
	<property name="errorHandler" instance="${webErrorHandler}" />
	<property name="pkgs">
		<list>
			<value>com.caibowen.web.controller</value>
		</list>
	</property>
</bean>
```
#####Part Three: Internationalization. 
add language packages 
	`en.properties` where there is `gplumeIsRunning=Gplume is Running!` 
and 
	`zh_CN.properties` where there is `gplumeIsRunning=Gplume \u8DD1\u8D77\u6765\u4E86\uFF01`(ascii for `Gplume跑起来了`)  

specify them in the manifest.xml
```XML
	<bean id="i18nService" class="com.caibowen.gplume.web.i18n.WebI18nService">
	    <property name="defaultLang" value="SimplifiedChinese"/>
	    <property name="pkgFiles">
	        <props>
	            <prop key="en">${lang_cn}</prop>
	            <prop key="${iso639_en}">/${i18base}/${i18_cn}</prop>
	        </props>
	    </property>
	    <property name="${time_zone_name}" value="ETC/GMT-8"/>
	</bean>
```
use message tag in JSP
```HTML
<label>
	<gp:msg k="gplumeIsRunning" />
</label>
```
#####Part Four: Gplume Web MVC
Pre-process and after-process requests
```java
public class SampleProcessor implements IRequestProcessor {
	@Override // all requests go through this
	public void process(RequestContext context) {
		before(context);
		getNext().process(context);
		after(context);
	}
```
Chaining processors in XML
```XML
<bean id="headPrePrcessor" class="com.caibowen.gplume.web.i18n.NativePkgInjector">
	<property name="i18nService" ref="i18nService"/>
	<property name="next">
		<bean class="com.caibowen.web.plugin.ui.BackgroundImgPreprocessor">
			<property name="next" instance="com.caibowen.web.plugin.statistic.LogRequestPreProcessor"/>
		</bean>
	</property>
</bean>
<bean class="com.caibowen.gplume.web.WebConfig">
	<property name="preProcessor" ref="headPrePrcessor"/>
	<property name="pkgs">
		<list>
			<value>com.caibowen.gplume.sample</value>
		</list>
	</property>
	<!--by default it is Jsp view resolver-->
	<property name="viewPrefix" value="/"/>
	<property name="viewSuffix" value=".jsp" />
</bean>
```
Handle request with method
```Java
	@Handle(value={"/", "/index",
			"/index.html", "/index.jsp"}})
	public String index(RequestContext context) {
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		return "index"; 
	}
	//number of concurrent requests is limited by this semapher
	@Semaphored(permit=100, fair=false, timeout=1000)// when time is out, send http code 503 
	@Handle(value={"/your-birthday/{date formate like 1992-6-14::Date}"}
			, httpMethods={HttpMethod.GET, HttpMethod.POST})
	public FreeMarkerView happyBirthday(Date date) {
		process(date);
		return new FreeMarkerView(date);
	}
```
Handle request with an object storing current state
```Java
//Warn: insecure login controller, for demo only
@Controller("/async/")// the base path
public class SampleController {
	@Inject Validator validator;
	@Inject PublicKeyService keyService;
	//state class can be defined any where, public or private, static or non-static
	class MyState {
		@ReqAttr(value="alias", defaultVal="1992-6-14")
		Date testdata1;
		@CookieVal(defaultVal=" 2.457  ", nullable=false)
		double testdata2;
		@Inject // injected after instanciation 
		@Named("myUserService")
		UserService userService;

		@ReqParam("psw_cipher")
		String passwordCipher;
		@ReqParam(value="email_address", nullable = false)
		String email;
		@SessionAttr(value="this_pubkey",nullable = false)
		PublicKey key;
		
		User user;
		boolean ok() {
			String psw = keyService.decrypt(key, passwordCipher);
			if (!Str.Utils.notBlank(psw)
					||!validator.matches(email, psw))
				return false;
			else {
				user = userService.getByEmail(email);
				return true;
			}
		}
	}
	
	@Semaphored(permit=300, timeout=2200)
	@Handle(value={"login"}, httpMethods={HttpMethod.POST})
	public IView login(MyState reqScope, RequestContext req) {
		if (reqScope == null) //non-null requirements are not met.
			return IView.get.textView("no public key in session");
		else if (!reqScope.ok())
			return IView.get.textView("password and email mismatch");
		else {
			req.session(true).setAttribute("this-user", reqScope.user);
			return IView.get.jump("/user/" + reqScope.user.getNameURL());
		}
	}
}
```
Intercept requests
```java
	@Intercept(value = { "/user*" }) // intercept request by URL
	public void demo(RequestContext context, IAction action) throws Throwable {
		if (hasLogedIn(context)) {
			action.perform(context);
			after(context);
		} else {
			context.jumpTo("/login");
		}
	}
```
#####Part Five. handle Event. 
register listeners and publish events as:
```Java
AppContext.broadcaster.register(new IEventHook() {
@Override
public void catches(AppEvent event) {
		LOG.info("cought event {0} from source {1}"
				, event.getClass().getSimpleName()
				, event.getSource());
	}
}, false);//set false to keep a weak reference to this listener
AppContext.broadcaster.register(new IAppListener<TimeChangedEvent>() {
@Override
public void onEvent(TimeChangedEvent event) {
		LOG.info("time changed {0}", event.getTime());
	}
});// default is true
TimeChangedEvent event = new TimeChangedEvent(this);
event.setTime(new Date());
AppContext.broadcaster.broadcast(event);
```
#####Part Six: ORM using Hibernate.
Spring and Hibernate can be integrated to Gplume with just a few lines configuration:

``` XML
<!-- will set sessionFactory bean with id ${sessionFactoryID} in afterPropertiesSet() -->
<bean class="com.caibowen.gplume.sample.test.SessionFactoryBuilder">
	<property name="sessionFactoryID" value="sessionFactory"/>
	<property name="dataSource" ref="dataSource" />
	<property name="hibernateProperties">
		<props>
			<prop key="hibernate.dialect">org.hibernate.dialect.MySQL5Dialect</prop>
		</props>
	</property>
	<property name="annotatedClasses">
		<list> <!-- set field "Class[] annotatedClasses" -->
			<value>com.caibowen.gplume.sample.model.Chapter</value>
		</list>
	</property>
</bean>
<!-- use HibernateDaoSupport-->
<bean id="chapterDao" class="com.caibowen.gplume.sample.dao.ChapterDAO">
	<property name="sessionFactory" ref="sessionFactory" />
</bean>
```
 and the ChaperDAO 
```Java
public class ChapterDAO extends HibernateDaoSupport {
	public List<Chapter> getAll() {
		return getHibernateTemplate().find("from Chapter");
	}
}
```
#####Part Seven. Test with Junit
```Java
@RunWith(JunitPal.class)
@ManifestPath("file:src/manifest.xml") // read as file
public class TestDemo {
	@Named("controlCenter")
	public AbstractControlCenter controlCenter;
	@Test
	public void test() {
		System.out.println(controlCenter);
		AppContext.broadcaster.broadcast(new WebAppStartedEvent(this));
	}
}
```
===========
**************
Gplume Overview
![alt text](https://dl.dropboxusercontent.com/s/iklpmr1jdyktdn2/gplume_struture.jpg)

***************
Project home www.caibowen.com/work.html#id_gplume
