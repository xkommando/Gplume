Gplume
======

light-weight Java web framework that cross-cloud

Quick Start:

web.xml
```XML
	<context-param>
        <param-name>manifest</param-name>
        <param-value>/WEB-INF/app-manifest.xml</param-value>
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

app-manifest.xml, similar to spring applicationContext.xml

```XML
	<bean id="i18nService" class="com.caibowen.gplume.web.i18n.WebI18nService">
	    <property name="defaultLang" value="SimplifiedChinese"/>
	</bean>
	<bean id="bootHelper" class="com.caibowen.gplume.web.i18n.WebAppBootHelper">
	    <property name="pkgFiles">
	        <list>
	            <value>/WEB-INF/i18n/en.properties</value>
	            <value>/WEB-INF/i18n/zh_CN.properties</value>
	        </list>
	    </property>
	    <property name="defaultTimeZone" value="ETC/GMT-8"/>
	</bean>
```
A web Controller:

```Java
	@Handle({"/Gplume/",
			"/Gplume/index",
			"/Gplume/index.html",
			"/Gplume/index.jsp"})
	public void index(RequestContext context) {
		
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		context.render("/index.jsp");
	}
```
For i18n, you need language packages like
`en.properties` where there is `gplumeIsRunning=Gplume is Running!`

and `zh_CN.properties` where there is `gplumeIsRunning=Gplume \u8DD1\u8D77\u6765\u4E86\uFF01` ascii for `Gplume跑起来了` and specify them in the manifest.xml

There is a Broadcaster, you can register listeners and publish events like
```Java
AppContext.broadcaster.register(new IEventHook() {
	@Override
	public void catches(AppEvent event) {
		LOG.info("cought event[" + event.getClass().getName() + "]"
				+ "from source[" + event.getSource() + "]");
	}
});
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
Overview
![alt text](https://dl.dropboxusercontent.com/s/eb07qh9ypr24fmi/gplume_structure.jpg)

***************
For more infomation, goto www.caibowen.com/blog/tag/pglume

Project can be find at www.caibowen.com/work.html#id_gplume
