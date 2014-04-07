/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.monitor.term;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple unit test for the Credential class.
 *
 */
public class CredentialTest {
	
	@Test
	public void testConstructors(){
		Credential dflt = new Credential();
		
		assertThat(dflt.getPass()).isNull();
		assertThat(dflt.getUser()).isNull();

		Credential constructor = new Credential("a", "a");
		
		assertThat(constructor.getPass()).isEqualTo("a");
		assertThat(constructor.getUser()).isEqualTo("a");
	}
	
	@Test
	public void testSetters(){
		Credential cre = new Credential();
		
		cre.setPass("a");
		cre.setUser("a");
		
		assertThat(cre.getPass()).isEqualTo("a");
		assertThat(cre.getUser()).isEqualTo("a");
	}
	
	@Test
	public void testHashCode(){
		Credential cre1 = new Credential("a", "a");
		Credential cre2 = new Credential("a", "a");
		
		assertThat(cre2.hashCode() == cre1.hashCode()).isTrue();
	}
	
	@Test
	public void testEqualsTo(){
		Credential cre1 = new Credential("a", "a");
		Credential cre2 = new Credential("a", "a");
		
		assertThat(cre1.equals(cre1)).isTrue();
		
		assertThat(cre1.equals(null)).isFalse();
		
		assertThat(cre1.equals(10)).isFalse();
		
		assertThat(cre1.equals(cre2)).isTrue();
		
		Credential creNull1 = new Credential();
		Credential creNull2 = new Credential();
		
		assertThat(creNull1.equals(creNull2)).isTrue();
		
		Credential crePassNull = new Credential("a", null);
		Credential creUserNull = new Credential(null, "a");
		
		assertThat(crePassNull.equals(cre1)).isFalse();
		assertThat(creUserNull.equals(cre1)).isFalse();
		
		Credential crebb = new Credential("b", "b");
		Credential creba = new Credential("b", "a");
		
		assertThat(cre1.equals(crebb)).isFalse();
		assertThat(cre1.equals(creba)).isFalse();
	}
}
