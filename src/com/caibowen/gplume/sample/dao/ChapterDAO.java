package com.caibowen.gplume.sample.dao;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.caibowen.gplume.sample.model.Chapter;


/**
 *@author BowenCai
 */
public class ChapterDAO extends HibernateDaoSupport {
	
	@SuppressWarnings("unchecked")
	public List<Chapter> getAll() {
		return getHibernateTemplate().find("from Chapter");
	}
}
