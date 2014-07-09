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
#####Part Zero: Configurate the web.xml:

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
#####Part One: The IoC Container.
Spring-like schema :
```XML
	<bean class="com.caibowen.gplume.web.WebConfig" >
		<property name="preProcessor" ref="headPrePrcessor"/>
		<property name="errorHandler" instance="com.caibowen.web.misc.ErrorHandler" />
		<property name="pkgs">
			<list>
				<value>com.caibowen.web.controller</value>
			</list>
		</property>
	</bean>
```
#####Part Two: Internationalization. 
add language packages 
	`en.properties` where there is `gplumeIsRunning=Gplume is Running!` 
and add
	`zh_CN.properties` where there is `gplumeIsRunning=Gplume \u8DD1\u8D77\u6765\u4E86\uFF01`(ascii for `Gplume跑起来了`)  

specify them in the manifest.xml
```XML
	<bean id="i18nService" class="com.caibowen.gplume.web.i18n.WebI18nService">
	    <property name="defaultLang" value="SimplifiedChinese"/>
	    <property name="pkgFiles">
	        <props>
	            <prop key="en">/i18n/en.properties</prop><!-- in your web root -->
	            <prop key="zh_CN">/i18n/zh_CN.properties</prop>
	        </props>
	    </property>
	    <property name="defaultTimeZone" value="ETC/GMT-8"/>
	</bean>
```
#####Part Three: Handle HTTP Request
handle request with a method
```Java
	@Handle(value={"/", "/index",
			"/index.html", "/index.jsp"}})
	public String index(RequestContext context) {
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		return "/index.jsp";
	}
	//number concurrent requests is limited by this semapher
	@Semaphored(permit=100, fair=false)
	@Handle(value={"/your-birthday/{date formate like 1992-6-14::Date}"}
			, httpMethods={HttpMethod.GET, HttpMethod.POST})
	public FreeMarkerView happyBirthday(Date date) {
		process(date);
		return new FreeMarkerView(date);
	}
```
handle request with an object storing current state
```Java
//Note: this sample login function is for demo only and is insecure
@Controller("/async/")// the base path
public class SampleController {
	@Inject Validator validator;
	@Inject PublicKeyService keyService;
	@Inject UserService userService;
	
	class MyState {
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
					||!validator.matchEmail(email, psw))
				return false;
			else {
				user = userService.getUser(email);
				return true;
			}
		}
	}
	
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
#####Part Four. handle Event. 
register listeners and publish events as:
```Java
AppContext.broadcaster.register(new IEventHook() {
@Override
public void catches(AppEvent event) {
		LOG.info("cought event {0} from source {1}"
				, event.getClass().getSimpleName()
				, event.getSource());
	}
});
AppContext.broadcaster.register(new IAppListener<TimeChangedEvent>() {
@Override
public void onEvent(TimeChangedEvent event) {
		LOG.info("time changed {0}", event.getTime());
	}
});
TimeChangedEvent event = new TimeChangedEvent(this);
event.setTime(new Date());
AppContext.broadcaster.broadcast(event);
```
#####Part Five: ORM using Hibernate.
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
#####Part Six. Test with Junit
```Java
@RunWith(JunitPal.class)
@ManifestPath("file:src/manifest.xml") // read as file
public class TestNULL {
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
