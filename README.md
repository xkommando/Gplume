Gplume
======

Light-weight multiparadigmatic Java web framework. Features:
**easy to learn and use,**
**mixed Spring styled request handling,**
**high performance,**
**cross cloud.**

**Quick Start:**

####Step Zero: The web.xml:

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
####Step One: The IoC Container.
Spring-like configuration file :

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
####Step Two: Handle request
```Java
	@Handle(value={"/",
			"/index",
			"/index.html",
			"/index.jsp"},
			httpMethods={HttpMethod.GET, HttpMethod.POST})
	public String index(RequestContext context) {
		
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		return "/index.jsp";
	}
	@Handle(value={"/your-birthday/{date formate like 1992-6-14::Date}"}
			, httpMethods={HttpMethod.GET, HttpMethod.POST})
	public FreeMarkerView happyBirthday(Date date, RequestContext context) {
		process(date);
		return new FreeMarkerView(date);
	}
```
####Step Three: Internationalization. 
Lets get cultural! For i18n, you need language packages like  
`en.properties` where there is `gplumeIsRunning=Gplume is Running!` 
and  
`zh_CN.properties` where there is `gplumeIsRunning=Gplume \u8DD1\u8D77\u6765\u4E86\uFF01`(ascii for `Gplume跑起来了`)  
and specify them in the manifest.xml

####Step Four. Event handling. 
There is a Broadcaster, you can register listeners and publish events like:

```Java
AppContext.broadcaster.register(new IEventHook() {
	@Override
	public void catches(AppEvent event) {
		LOG.info("cought event[" + event.getClass().getName() + "]"
				+ "from source[" + event.getSource() + "]");
	}
},false);//maintain a weak ref to this listener
AppContext.broadcaster.register(new IAppListener<TimeChangedEvent>() {
	@Override
	public void onEvent(TimeChangedEvent event) {
		LOG.info("time changed event");
	}
});
TimeChangedEvent event = new TimeChangedEvent(this);
event.setTime(new Date());
AppContext.broadcaster.broadcast(event);
```

####Step Five: ORM using Hibernate.
Spring and Hibernate can be integrated with Gplume, just a few lines in the xml:

``` XML
<bean id="datasource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
	<property name="driverClass" value="com.mysql.jdbc.Driver" />
	<!-- and other properties -->
</bean>
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
		<list>
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

####Step Six. Test with Junit
```Java
@RunWith(JunitPal.class)
@ManifestPath("file:src/manifest.xml") // as file
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
For more infomation, goto www.caibowen.com/blog/tag/pglume

Project can be find at www.caibowen.com/work.html#id_gplume
