/*
 * *****************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.common.test;

import com.caibowen.gplume.common.ImmutableArrayMap;
import com.caibowen.gplume.common.ImmutableArraySet;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ImmutableArrayGearsTest {
	
	@Test
	public void testSet() {
		ImmutableArraySet<String> arraySet = new ImmutableArraySet<String>(
				new Object[]{"v1", "v2", "v3", "v4"});
		Set<String> hashSet = new HashSet<String>();
		hashSet.add("v1");
		hashSet.add("v2");
		hashSet.add("v3");
		hashSet.add("v4");
		
		System.out.println("ImmutanbleArraySet.toString()");
		System.out.println(arraySet.toString());
		
		assertEquals(hashSet.size(), arraySet.size());
		
		assertEquals(hashSet.hashCode(), arraySet.hashCode());
		assertTrue(hashSet.equals(arraySet));
		assertTrue(arraySet.equals(hashSet));
		assertTrue(hashSet.containsAll(arraySet));
		assertTrue(arraySet.containsAll(hashSet));
		
		List<Object> hashList = Arrays.asList(hashSet.toArray());
		List<Object> arrayList = Arrays.asList(arraySet.toArray());
		
		assertEquals(hashList.size(), arrayList.size());
		assertTrue(hashList.containsAll(arrayList));
		assertTrue(arrayList.containsAll(hashList));

		assertEquals(hashSet.contains("v1"), arraySet.contains("v1"));
		assertEquals(hashSet.contains("no such key"), arraySet.contains("no such key"));
	}
	
	@Test
	public void testMap() {
		ImmutableArrayMap<String, String> arrayMap
		= new ImmutableArrayMap<String, String>(new Object[][]
			{
			{"k1", "v1"},
			{"k2", "v2"},
			{"k3", "v3"},
			{null, null},
			});
	HashMap<String, String> hashMap = new HashMap<String, String>();
	hashMap.put("k1", "v1");
	hashMap.put("k2", "v2");
	hashMap.put("k3", "v3");
	hashMap.put(null, null);
	
	System.out.println("ImmutableArrayMap.toString()");
	System.out.println(arrayMap.toString());
	
	assertEquals(hashMap.size(), arrayMap.size());
	
	assertEquals(hashMap.hashCode(), arrayMap.hashCode());
	assertTrue(hashMap.equals(arrayMap));
	assertTrue(arrayMap.equals(hashMap));
	
	assertTrue(hashMap.keySet().equals(arrayMap.keySet()));
	assertTrue(arrayMap.keySet().equals(hashMap.keySet()));
	
	assertTrue(hashMap.entrySet().equals(arrayMap.entrySet()));
	assertTrue(arrayMap.entrySet().equals(hashMap.entrySet()));
	
	List<String> hashList = new ArrayList<String>(hashMap.values());
	List<String> arrayList = new ArrayList<String>(arrayMap.values());

	assertEquals(hashList.size(), arrayList.size());
	assertTrue(hashList.containsAll(arrayList));
	assertTrue(arrayList.containsAll(hashList));
	
	
	assertEquals(hashMap.get("k1"), arrayMap.get("k1"));
	assertTrue(arrayMap.get("no such key") == null);
	assertTrue(hashMap.get("no such key") == arrayMap.get("no such key"));
	
	}

}
