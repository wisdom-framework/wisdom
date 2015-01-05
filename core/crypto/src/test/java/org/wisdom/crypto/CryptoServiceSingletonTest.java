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
package org.wisdom.crypto;

import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.crypto.Hash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Checks the Crypto Service Implementation.
 */
public class CryptoServiceSingletonTest {

    public static final String SECRET = "JYFVq6:^jrh:KIy:yM5Xb<sH58WW80OLL4_gCL4Ne[PnAJ9QC/Z?LG2dbwoSkiBL";

    Crypto crypto;
    ApplicationConfiguration configuration;

    @Before
    public void setUp() {
        configuration = mock(ApplicationConfiguration.class);
        when(configuration.getOrDie(ApplicationConfiguration.APPLICATION_SECRET)).thenReturn(SECRET);
        when(configuration.getWithDefault("crypto.default-hash", "MD5")).thenReturn("MD5");
        when(configuration.getIntegerWithDefault("crypto.aes.key-size", 128)).thenReturn(128);
        when(configuration.getIntegerWithDefault("crypto.aes.iterations", 20)).thenReturn(20);
        when(configuration.getWithDefault("crypto.aes.transformation", Crypto.AES_CBC_ALGORITHM))
                .thenReturn(Crypto.AES_CBC_ALGORITHM);

        crypto = new CryptoServiceSingleton(configuration);
    }

    @Test
    public void testMD5() throws Exception {
        String s = crypto.hash("hello");
        assertThat(s).isEqualTo("XUFAKrxLKna5cZ2REBfFkg==");
    }

    @Test
    public void testWishSha1AsDefault() {
        when(configuration.getWithDefault("crypto.default-hash", "MD5")).thenReturn("SHA1");
        crypto = new CryptoServiceSingleton(configuration);
        assertThat(crypto.hash("hello")).isEqualTo("qvTGHdzF6KLavt4PO0gs2a6pQ00=");
    }

    @Test
    public void testSha1() throws Exception {
        String s = crypto.hash("hello", Hash.SHA1);
        assertThat(s).isEqualTo("qvTGHdzF6KLavt4PO0gs2a6pQ00=");
    }

    @Test
    public void testSha256() throws Exception {
        String s = crypto.hash("hello", Hash.SHA256);
        assertThat(s).isEqualTo("LPJNul+wow4m6DsqxbninhsWHlwfp0JecwQzYpOLmCQ=");
    }

    @Test
    public void testSha512() throws Exception {
        String s = crypto.hash("hello", Hash.SHA512);
        assertThat(s).isEqualTo("m3HSJL1i83hdltRq0+o9czGb+8KJDKra4t/3JRlnPKcjI8PZm6XBHXx6zG4UuMXaDEZjR1wuXDre9G9zvN7AQw==");
    }

    @Test
    public void testSign() {
        String s = crypto.sign("hello");
        assertThat(s).isEqualTo("64f2c3cbb5bf009e47c97bdc12973324b8a271d7");
    }

    @Test
    public void testAES() {
        String s = crypto.encryptAES("hello");
        assertThat(s).isEqualTo("4d72b2e01f589382a1b9fec63fa0f59b");

        String s2 = crypto.decryptAES(s);
        assertThat(s2).isEqualTo("hello");
    }

    @Test
    public void testAESEncryptDecryptCycles() {
        String s = "Wisdom Framework";
        String key = "0123456789abcdef";

        assertThat(crypto.decryptAES(crypto.encryptAES(s, key), key)).isEqualTo(s);
        assertThat(crypto.decryptAES(crypto.encryptAES(s))).isEqualTo(s);
    }

    @Test
    public void testAES_CTR_NoPadding() {
        String s = "Wisdom Framework";
        String key = "0123456789abcdef";

        when(configuration.getWithDefault("crypto.aes.transformation", Crypto.AES_CBC_ALGORITHM))
                .thenReturn("AES/CTR/NoPadding");

        crypto = new CryptoServiceSingleton(configuration);
        assertThat(crypto.decryptAES(crypto.encryptAES(s, key), key)).isEqualTo(s);
        assertThat(crypto.decryptAES(crypto.encryptAES(s))).isEqualTo(s);
    }

    @Test
    public void testAESWithSaltUsingDefaultIV() {
        final String salt = "0000000000000000";
        String s = crypto.encryptAESWithCBC("hello", salt);
        String s2 = crypto.decryptAESWithCBC(s, salt);
        assertThat(s2).isEqualTo("hello");
    }

    @Test
    public void testAESWithSalt() {
        String secret = "7/19T8CiU@paf[9bF7ll<1/5@P:7xBQhFkxx??9ALJ[3B<cjoKm_k50yA_Ib2uT2";
        String vector = "b02132081808b493c61e86626ee6c2e2";
        final String salt = "0000000000000000";
        String s = crypto.encryptAESWithCBC("hello", secret.substring(0, 16), salt, vector);
        String r = crypto.decryptAESWithCBC(s, secret.substring(0, 16), salt, vector);
        assertThat(r).isEqualTo("hello");
    }

    @Test
    public void testTokenSignature() {
        String raw = "hello";
        String encrypted = crypto.signToken(raw);
        String extracted = crypto.extractSignedToken(encrypted);
        assertThat(extracted).isEqualTo(raw);
    }

    @Test
    public void testBase64() {
        String s = "hello";
        String s1 = crypto.encodeBase64(s.getBytes());
        assertThat(s1).isEqualTo("aGVsbG8=");
        byte[] s2 = crypto.decodeBase64(s1);
        assertThat(new String(s2)).isEqualTo(s);
    }

    @Test
    public void testHexMD5() {
        String s = "hello";
        String s1 = crypto.hexMD5(s);
        assertThat(s1).isEqualTo("5d41402abc4b2a76b9719d911017c592");
    }

    @Test
    public void testHexSha1() {
        String s = "hello";
        String s1 = crypto.hexSHA1(s);
        assertThat(s1).isEqualTo("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");
    }

    @Test
    public void testTokenGeneration() {
        String token = crypto.generateToken();
        assertThat(token).isNotNull().isNotEmpty();
        String token2 = crypto.generateToken();
        assertThat(token2).isNotNull().isNotEmpty();
        assertThat(token).isNotEqualTo(token2);
    }

    @Test
    public void testSignedTokenGeneration() {
        String token = crypto.generateSignedToken();
        assertThat(token).isNotNull().isNotEmpty();
        String token2 = crypto.generateSignedToken();
        assertThat(token2).isNotNull().isNotEmpty();
        assertThat(token).isNotEqualTo(token2);

        assertThat(crypto.compareSignedTokens(token2, token)).isFalse();
    }
}
