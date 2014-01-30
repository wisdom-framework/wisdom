package org.wisdom.wisit.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.wisdom.wisit.auth.Credential;

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
