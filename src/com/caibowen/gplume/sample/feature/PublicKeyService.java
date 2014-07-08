package com.caibowen.gplume.sample.feature;

import java.security.Key;
import java.security.PublicKey;


/**
 * 
 * @author BowenCai
 *
 */
public interface PublicKeyService {

	public PublicKey getPublicKey(String id);
	
	public String decrypt(PublicKey key, String cipher);
}
