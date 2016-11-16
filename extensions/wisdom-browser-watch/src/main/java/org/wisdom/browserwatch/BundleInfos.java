package org.wisdom.browserwatch;

import java.util.Set;
import java.util.TreeSet;

public class BundleInfos {
	private final Set<String> controllerClassNames = new TreeSet<String>();

	public Set<String> getControllerClassNames() {
		return controllerClassNames;
	}

	public void addController(String sourceClassName) {
		this.controllerClassNames.add(sourceClassName);
	}
}
