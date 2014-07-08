package com.caibowen.gplume.web.view;

import java.io.PrintWriter;

import com.caibowen.gplume.web.RequestContext;

public class TextView implements IView {

	private final String encoding;
	private final String type;
	public TextView() {
		this.encoding = PageAttributes.Encoding.UTF_8;
		this.type = PageAttributes.Type.TEXT;
	}
	
	public TextView(String encoding, String type) {
		this.encoding = encoding;
		this.type = type;
	}

	private String content;

	public TextView setContent(String content) {
		this.content = content;
		return this;
	}

	@Override
	public void resolve(RequestContext context) {
		try {
			context.response.setContentType(type);
			context.response.setCharacterEncoding(encoding);
			PrintWriter writer = context.response.getWriter();
			writer.write(content);
		} catch (Exception e) {
			throw new RuntimeException("Error writing JSP", e);
		}
	}

}
