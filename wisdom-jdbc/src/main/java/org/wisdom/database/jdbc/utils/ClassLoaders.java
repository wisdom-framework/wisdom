package org.wisdom.database.jdbc.utils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to load classes from any bundle exporting the package containing the class.
 * Be aware: this is not the right OSGi way.
 */
public class ClassLoaders {

    private ClassLoaders() {
        // Avoid direct instantiation.
    }

    /**
     * Load the Class of name <code>classname</code>
     * TODO : handle class version
     *
     * @param context   The BundleContext
     * @param classname The fully qualified class name
     * @return The Class of name <code>classname</code>
     * @throws ClassNotFoundException if we can't load the Class of name <code>classname</code>
     */
    public static Class<?> loadClass(BundleContext context, String classname) throws ClassNotFoundException {
        // Before attempting to do some fancy lookup, try to load the class from the current bundle, we never know...
        try {
            return context.getBundle().loadClass(classname);
        } catch (ClassNotFoundException e) {
            // No luck, will look into exporters.
        }

        // extract package name
        String packageName = classname.substring(0, classname.lastIndexOf('.'));
        BundleCapability exportedPackage = getExportedPackage(context, packageName);
        if (exportedPackage == null) {
            throw new ClassNotFoundException("No package found with name " + packageName + " while trying to load the class "
                    + classname + ".");
        }
        return exportedPackage.getRevision().getBundle().loadClass(classname);
    }

    /**
     * Return the BundleCapability of a bundle exporting the package packageName.
     *
     * @param context     The BundleContext
     * @param packageName The package name
     * @return the BundleCapability of a bundle exporting the package packageName
     */
    private static BundleCapability getExportedPackage(BundleContext context, String packageName) {
        List<BundleCapability> packages = new ArrayList<>();
        for (Bundle bundle : context.getBundles()) {
            BundleRevision bundleRevision = bundle.adapt(BundleRevision.class);
            for (BundleCapability packageCapability : bundleRevision.getDeclaredCapabilities(BundleRevision.PACKAGE_NAMESPACE)) {
                String pName = (String) packageCapability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                if (pName.equalsIgnoreCase(packageName)) {
                    packages.add(packageCapability);
                }
            }
        }

        Version max = Version.emptyVersion;
        BundleCapability maxVersion = null;
        for (BundleCapability aPackage : packages) {
            Version version = (Version) aPackage.getAttributes().get("version");
            if (max.compareTo(version) <= 0) {
                max = version;
                maxVersion = aPackage;
            }
        }

        return maxVersion;
    }
}
