package uk.gov.justice.log.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A factory for Properties with override capabilities The default properties are loaded from the classpath, 
 * an overrides file can be loaded from the file system too.
 * This class deliberately swallows exceptions when files are missing.
 * 
 * @author bdellegrazie
 *
 */
public class PropertiesFactory {
    public static Properties safeLoadDefaultsAndOverrides(String defaults_resource, String overrides_file) {
        return safeLoadFromFile(overrides_file, safeLoadFromClasspath(defaults_resource));
    }

    public static Properties safeLoadFromInputStream(InputStream is, Properties defaults) {
        Properties props = new Properties(defaults);

        try {
            props.load(is);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                } finally {
                    is = null;
                }
            }
        }

        return props;
    }

    public static Properties safeLoadFromFile(String file, Properties defaults) {
        Properties props = new Properties(defaults);

        try {
            props = safeLoadFromInputStream(new FileInputStream(file), defaults);
        } catch (IOException e) {
        }

        return props;
    }

    public static Properties safeLoadFromFile(String file) {
        return safeLoadFromFile(file, null);
    }

    public static Properties safeLoadFromClasspath(String resource, Properties defaults) {
        return safeLoadFromInputStream(PropertiesFactory.class.getResourceAsStream(resource), defaults);
    }

    public static Properties safeLoadFromClasspath(String resource) {
        return safeLoadFromClasspath(resource, null);
    }
}
