package com.caibowen.gplume.sample.controller;

import com.caibowen.gplume.web.IRequestProcessor;
import com.caibowen.gplume.web.RequestContext;

public class SampleProcessor implements IRequestProcessor {
	
	@Override
	public void process(RequestContext context) {
		before(context);
		getNext().process(context);
		after(context);
	}
	
	
	public void before(RequestContext context) {
		
	}
	
	public void after(RequestContext context) {
		
	}
	
	@Override
	public IRequestProcessor getNext() {
		return null;
	}

	@Override
	public void setNext(IRequestProcessor preProcessor) {
	}
}
