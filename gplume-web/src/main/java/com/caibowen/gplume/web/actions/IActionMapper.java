package com.caibowen.gplume.web.actions;

import com.caibowen.gplume.web.IAction;

import javax.annotation.Nullable;

/**
 *
 *  handlers manage actions, and pass request,
 * along with its response, to the correspondent actor and the actor performs
 *
 *
 * Created by Bowen Cai on 12/31/2014.
 */
public interface IActionMapper<T extends IAction> {

	void add(String uri, T action) throws IllegalArgumentException;

	boolean remove(String uri);

	@Nullable
	IAction getAction(String uri);

	void clear();
}
