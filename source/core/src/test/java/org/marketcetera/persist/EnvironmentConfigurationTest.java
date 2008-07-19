package org.marketcetera.persist;

import org.marketcetera.core.ClassVersion;
import org.marketcetera.util.log.SLF4JLoggerProxy;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Enumeration;
import java.util.Properties;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/* $License$ */
/**
 * This test verifies that the correct versions of different
 * softwares, that we depend on, are being used.
 * These tests are only meant to ensure that correct
 * software versions are used in build / dev environment, to help save
  * time when wrong versions are used by error.
 * These tests are not meant to indicate that we have strong 
 * dependency on the specific versions of these softwares, the 
 * persistence infrastructure is depends on the JPA specification
 * and in no way is dependent on a specific version / implementation of
 * JPA provider or the database.
 *
 * @author anshul@marketcetera.com
 */
@ClassVersion("$Id$")
public class EnvironmentConfigurationTest extends PersistTestBase {
    @Test
    public void hibernate() throws Exception {
        //Fetch the hibernate version number from the jar's manifest
        Enumeration<URL> res = ClassLoader.getSystemResources("META-INF/MANIFEST.MF");
        boolean foundCore = false;
        boolean foundEntityMgr = false;
        while(res.hasMoreElements()) {
            URL url = res.nextElement();
            SLF4JLoggerProxy.debug(this,url.toString());
            if(url.getPath().indexOf("/hibernate/hibernate/") >= 0) {
                verifyPropertyValue(url,"Hibernate-Version","3.2.6.ga");
                foundCore = true;
            }
            if(url.getPath().indexOf("/hibernate-entitymanager/") >= 0) {
                verifyPropertyValue(url,"Implementation-Version","3.3.2.GA");
                foundEntityMgr = true;
            }
        }
        assertTrue("Hibernate core library not found",foundCore);
        assertTrue("Hibernate entity manager library not found", foundEntityMgr);
    }

    /**
     * Tests the mysql version number and configuration.
     *
     * @throws Exception if there was an error fetching the mysql
     * version number
     */
    @Test
    public void mysql() throws Exception {
        DataTypes dt = new DataTypes();
        final String version = dt.fetchDBVersion();
        SLF4JLoggerProxy.debug(this,version);
        assertTrue("Unexpected MySQL version: "+version,
                version.indexOf("5.0.51") >= 0);
        assertEquals("Unexpected database charset","utf8",dt.fetchDBCharset());
        assertEquals("Unexpected connection charset","utf8",dt.fetchConnCharset());
        assertEquals("Unexpected client charset","utf8",dt.fetchClientCharset());
        assertEquals("Unexpected database collation","utf8_general_ci",
                dt.fetchDBCollation());
        assertEquals("Unexpected connection collation","utf8_general_ci",
                dt.fetchDBConnCollation());
        assertEquals("Unexpected connection timezone","+00:00",dt.fetchDBConnTz());
        assertEquals("Unexpected locale","en_US",dt.fetchDBLocale());
    }
    @BeforeClass
    public static void setup() throws Exception {
        springSetup(new String[]{"persist.xml"});
    }

    /**
     * Verifies that the properties file pointed to by the supplied URL
     * has the specified property name value pair
     *
     * @param url the URL of the properties file
     * @param propertyName the property name
     * @param propertyValue the property value
     * 
     * @throws IOException if there was an error reading the properties file.
     */
    private void verifyPropertyValue(URL url,
                               String propertyName,
                               String propertyValue) throws IOException {
        Properties p = new Properties();
        InputStream inStream = url.openStream();
        try {
            p.load(inStream);
            assertEquals("Incorrect library version number in library " + url,
                    propertyValue,
                    p.getProperty(propertyName));
        } finally {
            inStream.close();
        }
    }
}