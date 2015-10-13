/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.maven.utils;

import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MojoUtils {

    public static ProxyConfig getProxyConfig(MavenSession mavenSession, SettingsDecrypter decrypter) {
        if (mavenSession == null ||
                mavenSession.getSettings() == null ||
                mavenSession.getSettings().getProxies() == null ||
                mavenSession.getSettings().getProxies().isEmpty()) {
            return new ProxyConfig(Collections.<ProxyConfig.Proxy>emptyList());
        } else {
            final List<Proxy> mavenProxies = mavenSession.getSettings().getProxies();

            final List<ProxyConfig.Proxy> proxies = new ArrayList<>(mavenProxies.size());

            for (Proxy mavenProxy : mavenProxies) {
                if (mavenProxy.isActive()) {
                    mavenProxy = decryptProxy(mavenProxy, decrypter);
                    proxies.add(new ProxyConfig.Proxy(mavenProxy.getId(), mavenProxy.getProtocol(), mavenProxy.getHost(),
                            mavenProxy.getPort(), mavenProxy.getUsername(), mavenProxy.getPassword(), mavenProxy.getNonProxyHosts()));
                }
            }

            return new ProxyConfig(proxies);
        }
    }

    private static Proxy decryptProxy(Proxy proxy, SettingsDecrypter decrypter) {
        final DefaultSettingsDecryptionRequest decryptionRequest =
                new DefaultSettingsDecryptionRequest(proxy);
        SettingsDecryptionResult decryptedResult = decrypter.decrypt(decryptionRequest);
        return decryptedResult.getProxy();
    }
}
