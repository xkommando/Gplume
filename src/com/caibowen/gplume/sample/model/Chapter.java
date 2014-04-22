/*******************************************************************************
 * Copyright (c) 2014 WangKang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *    WangKang. - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.sample.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="wk_chapter")
public class Chapter implements Serializable {

	private static final long serialVersionUID = -1959747962303882199L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int 				id;
	
	@Column
	public Timestamp 		time_created;
	
	@Column
	public String 			name;
	
	@Column
	public String 			description;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getTime_created() {
		return time_created;
	}
	public void setTime_created(Timestamp time_created) {
		this.time_created = time_created;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((time_created == null) ? 0 : time_created.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chapter other = (Chapter) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (time_created == null) {
			if (other.time_created != null)
				return false;
		} else if (!time_created.equals(other.time_created))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Chapter [id=" + id + ", time_created=" + time_created
				+ ", name=" + name + ", description=" + description + "]";
	}
}
