/**
 * stateful actions
 */
/**
 * Why use many small action objects instead of a big one with some logic?
 * speed!
 * 
 * Note that IAction is Serializable to enable hot migration of applications 
 * Note that stateful actions does not support rest API
 * @author BowenCai
 *
 */
package com.caibowen.gplume.web.actions.stateful.actions;