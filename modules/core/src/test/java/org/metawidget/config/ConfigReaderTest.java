// Metawidget
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.metawidget.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import junit.framework.TestCase;

import org.metawidget.config.TestInspectorConfig.FooEnum;
import org.metawidget.iface.MetawidgetException;
import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.iface.Inspector;
import org.metawidget.inspector.xml.XmlInspector;
import org.metawidget.util.IOUtils;
import org.metawidget.util.LogUtils;
import org.metawidget.util.LogUtilsTest;

/**
 * @author Richard Kennard
 */

public class ConfigReaderTest
	extends TestCase {

	//
	// Public methods
	//

	public void testNoDefaultConstructor()
		throws Exception {

		// With config hint

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget>";
		xml += "	<xmlInspector xmlns=\"java:org.metawidget.inspector.xml\"/>";
		xml += "</metawidget>";

		ConfigReader configReader = new ConfigReader();

		try {
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "class org.metawidget.inspector.xml.XmlInspector does not have a default constructor. Did you mean config=\"XmlInspectorConfig\"?".equals( e.getMessage() ) );
		}

		// With out-of-package config hint

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget>";
		xml += "	<testOutOfPackageConfigInspector xmlns=\"java:org.metawidget.config.subpackage\"/>";
		xml += "</metawidget>";

		try {
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertEquals( "class org.metawidget.config.subpackage.TestOutOfPackageConfigInspector does not have a default constructor. Did you mean config=\"org.metawidget.config.TestInspectorConfig\"?", e.getMessage() );
		}

		// Without config hint

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget>";
		xml += "	<class xmlns=\"java:java.lang\"/>";
		xml += "</metawidget>";

		try {
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), Class.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "class java.lang.Class does not have a default constructor".equals( e.getMessage() ) );
		}
	}

	public void testBadUrl()
		throws Exception {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget>";
		xml += "	<xmlInspector xmlns=\"java:org.metawidget.inspector.xml\" config=\"XmlInspectorConfig\">";
		xml += "		<inputStream>";
		xml += "			<url>http://foo.nowhere</url>";
		xml += "		</inputStream>";
		xml += "	</xmlInspector>";
		xml += "</metawidget>";

		ConfigReader configReader = new ConfigReader();

		try {
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			String message = e.getMessage();

			// If, bizzarely, the host actually does resolve (maybe your ISP puts in a special
			// page), you'll get a FileNotFoundException

			assertTrue( "java.net.UnknownHostException: foo.nowhere".equals( message ) || "java.io.FileNotFoundException: http://foo.nowhere".equals( message ) );
		}
	}

	public void testBadFile()
		throws Exception {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget>";
		xml += "	<xmlInspector xmlns=\"java:org.metawidget.inspector.xml\" config=\"XmlInspectorConfig\">";
		xml += "		<inputStream>";
		xml += "			<file>/tmp/no.such.file</file>";
		xml += "		</inputStream>";
		xml += "	</xmlInspector>";
		xml += "</metawidget>";

		ConfigReader configReader = new ConfigReader();

		try {
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( e.getMessage().startsWith( "java.io.FileNotFoundException:" ) );
		}
	}

	public void testForgottenConfigAttribute()
		throws Exception {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<propertyTypeInspector xmlns=\"java:org.metawidget.inspector.propertytype\">";
		xml += "<propertyStyle><javaBeanPropertyStyle xmlns=\"java:org.metawidget.inspector.impl.propertystyle.javabean\"/></propertyStyle>";
		xml += "</propertyTypeInspector></metawidget>";

		try {
			ConfigReader configReader = new ConfigReader();
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertEquals( "java.lang.NoSuchMethodException: class org.metawidget.inspector.propertytype.PropertyTypeInspector.setPropertyStyle(JavaBeanPropertyStyle)", e.getMessage() );
		}
	}

	public void testLikelyMethod()
		throws Exception {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<propertyTypeInspector xmlns=\"java:org.metawidget.inspector.propertytype\" config=\"org.metawidget.inspector.impl.BaseObjectInspectorConfig\">";
		xml += "<propertyStyle><boolean>true</boolean></propertyStyle>";
		xml += "</propertyTypeInspector></metawidget>";

		try {
			ConfigReader configReader = new ConfigReader();
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertEquals( "java.lang.NoSuchMethodException: class org.metawidget.inspector.impl.BaseObjectInspectorConfig.setPropertyStyle(Boolean). Did you mean setPropertyStyle(PropertyStyle)?", e.getMessage() );
		}

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<xmlInspector xmlns=\"java:org.metawidget.inspector.xml\" config=\"org.metawidget.inspector.xml.XmlInspectorConfig\">";
		xml += "<inputStreams><list><int>0</int></list></inputStreams>";
		xml += "</xmlInspector></metawidget>";

		try {
			ConfigReader configReader = new ConfigReader();
			configReader.configure( new ByteArrayInputStream( xml.getBytes() ), XmlInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertEquals( "java.lang.NoSuchMethodException: class org.metawidget.inspector.xml.XmlInspectorConfig.setInputStreams(ArrayList). Did you mean setInputStreams(InputStream[])?", e.getMessage() );
		}
	}

	public void testSupportedTypes() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestInspectorConfig\">";
		xml += "<int><int>3</int></int>";
		xml += "<constant><constant>CONSTANT_VALUE</constant></constant>";
		xml += "<externalConstant><constant>javax.swing.SwingConstants.LEFT</constant></externalConstant>";
		xml += "<list>";
		xml += "<list>";
		xml += "<string>foo</string>";
		xml += "<string>bar</string>";
		xml += "<class>java.lang.String</class>";
		xml += "<class>java.util.Date</class>";
		xml += "<class>java.lang.Long</class>";
		xml += "<null/>";
		xml += "</list>";
		xml += "</list>";
		xml += "<set>";
		xml += "<set>";
		xml += "<string>baz</string>";
		xml += "</set>";
		xml += "</set>";
		xml += "<boolean><boolean>true</boolean></boolean>";
		xml += "<pattern><pattern>.*?</pattern></pattern>";
		xml += "<inputStream><resource>org/metawidget/config/metawidget-test-caching.xml</resource></inputStream>";
		xml += "<resourceBundle><bundle>org/metawidget/config/Resources</bundle></resourceBundle>";
		xml += "<stringArray><array><string>foo</string><string>bar</string></array></stringArray>";
		xml += "<enum><enum>BAR</enum></enum>";
		xml += "</testInspector>";
		xml += "</metawidget>";
		
		TestInspector inspector = new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
		assertTrue( 3 == inspector.getInt() );
		assertTrue( TestInspectorConfig.CONSTANT_VALUE == inspector.getConstant() );
		assertTrue( SwingConstants.LEFT == inspector.getExternalConstant() );

		List<Object> list = inspector.getList();
		assertTrue( "foo".equals( list.get( 0 ) ) );
		assertTrue( "bar".equals( list.get( 1 ) ) );
		assertTrue( String.class.equals( list.get( 2 ) ) );
		assertTrue( Date.class.equals( list.get( 3 ) ) );
		assertTrue( Long.class.equals( list.get( 4 ) ) );
		assertTrue( null == list.get( 5 ) );
		assertTrue( 6 == list.size() );

		Set<Object> set = inspector.getSet();
		assertTrue( "baz".equals( set.iterator().next() ) );

		assertTrue( true == inspector.isBoolean() );
		assertTrue( ".*?".equals( inspector.getPattern().toString() ) );

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		IOUtils.streamBetween( inspector.getInputStream(), streamOut );
		assertTrue( streamOut.toString().contains( "<metawidget xmlns=\"http://metawidget.org\"" ) );

		assertTrue( "Limited textbox (i18n)".equals( inspector.getResourceBundle().getString( "limitedTextbox" ) ) );

		assertTrue( 2 == inspector.getStringArray().length );
		assertTrue( "foo".equals( inspector.getStringArray()[0] ) );
		assertTrue( "bar".equals( inspector.getStringArray()[1] ) );

		assertTrue( FooEnum.BAR.equals( inspector.getEnum() ) );
	}

	public void testUnsupportedType() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestInspectorConfig\">";
		xml += "<date><date>1/1/2001</date></date>";
		xml += "</testInspector>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( e.getMessage().endsWith( "No such class org.metawidget.config.Date or supported tag <date>" ) );
		}
	}

	public void testBadNamesapce() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"org.metawidget.config\" config=\"TestInspectorConfig\">";
		xml += "<date><date>1/1/2001</date></date>";
		xml += "</testInspector>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "org.xml.sax.SAXException: Namespace 'org.metawidget.config' of element <testInspector> must start with java:".equals( e.getMessage() ) );
		}
	}

	public void testEmptyCollection() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestInspectorConfig\">";
		xml += "<list>";
		xml += "<list/>";
		xml += "</list>";
		xml += "<set>";
		xml += "<set/>";
		xml += "</set>";
		xml += "</testInspector>";
		xml += "</metawidget>";

		TestInspector inspector = new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
		assertTrue( inspector.getList().isEmpty() );
		assertTrue( inspector.getSet().isEmpty() );
	}

	public void testMetawidgetExceptionDuringConstruction() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestInspectorConfig\">";
		xml += "<failDuringConstruction><boolean>true</boolean></failDuringConstruction>";
		xml += "</testInspector>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "Failed during construction".equals( e.getCause().getMessage() ) );
		}
	}

	public void testSetterWithNoParameters() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestInspectorConfig\">";
		xml += "<noParameters/>";
		xml += "</testInspector>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "java.lang.UnsupportedOperationException: Called setNoParameters".equals( e.getMessage() ) );
		}
	}

	public void testNoInspector() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "No match for class org.metawidget.config.TestInspector within config".equals( e.getMessage() ) );
		}
	}

	public void testMultipleInspectors() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestInspectorConfig\"/>";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestInspectorConfig\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "Already configured a class org.metawidget.config.TestInspector, ambiguous match with class org.metawidget.config.TestInspector".equals( e.getMessage() ) );
		}
	}

	public void testMissingResource() {

		ConfigReader configReader = new ConfigReader();

		try {
			configReader.configure( (String) null, null );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "java.io.FileNotFoundException: No resource specified".equals( e.getMessage() ) );
		}

		try {
			configReader.configure( "", null );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "java.io.FileNotFoundException: No resource specified".equals( e.getMessage() ) );
		}

		try {
			configReader.configure( " ", null );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "java.io.FileNotFoundException: No resource specified".equals( e.getMessage() ) );
		}

		try {
			configReader.configure( " foo", null );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "java.io.FileNotFoundException: Unable to locate  foo on CLASSPATH".equals( e.getMessage() ) );
		}
	}

	public void testLogging() {

		ConfigReader configReader = new ConfigReader();
		configReader.configure( "org/metawidget/config/metawidget-test-logging.xml", CompositeInspector.class, "inspectors", "array" );
		configReader.configure( "org/metawidget/config/metawidget-test-logging.xml", Inspector.class, "inspectors", "array" );

		// Test it doesn't log 'Instantiated immutable class
		// org.metawidget.inspector.composite.CompositeInspector' a second time

		if ( LogUtils.getLog( ConfigReader.class ).isDebugEnabled() ) {
			assertEquals( "Reading resource from org/metawidget/config/metawidget-test-logging.xml/org.metawidget.inspector.iface.Inspector/inspectors/array", LogUtilsTest.getLastDebugMessage() );
		} else {
			assertTrue( !LogUtils.getLog( ConfigReader.class ).isDebugEnabled() );
			assertEquals( "Reading resource from {0}", LogUtilsTest.getLastDebugMessage() );
			assertEquals( "org/metawidget/config/metawidget-test-logging.xml/org.metawidget.inspector.iface.Inspector/inspectors/array", LogUtilsTest.getLastDebugArguments()[0] );
		}
	}

	public void testPatternCache()
		throws Exception {

		assertFalse( Pattern.compile( "foo" ).equals( Pattern.compile( "foo" ) ) );

		ConfigReader configReader = new ConfigReader();
		assertTrue( configReader.createNative( "pattern", null, "foo" ).equals( configReader.createNative( "pattern", null, "foo" ) ) );
	}

	public void testUppercase() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<TestInspector xmlns=\"java:org.metawidget.config\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "XML node 'TestInspector' should start with a lowercase letter".equals( e.getMessage() ) );
		}
	}

	public void testBadConfigImplementation() {

		// No equals

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestNoEqualsInspectorConfig\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "class org.metawidget.config.TestNoEqualsInspectorConfig does not override .equals(), so cannot cache reliably".equals( e.getMessage() ) );
		}

		// No hashCode

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestNoHashCodeInspectorConfig\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "class org.metawidget.config.TestNoHashCodeInspectorConfig does not override .hashCode(), so cannot cache reliably".equals( e.getMessage() ) );
		}

		// Unbalanced

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestUnbalancedEqualsInspectorConfig\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );

			// assertTrue( false );
			//
			// (works running JUnit in Eclipse, but not via Ant. Does the VM cache reflection
			// results or something?)
		} catch ( MetawidgetException e ) {
			assertTrue( "class org.metawidget.config.TestNoHashCodeInspectorConfig implements .equals(), but .hashCode() is implemented by class org.metawidget.config.TestUnbalancedEqualsInspectorConfig, so cannot cache reliably".equals( e.getMessage() ) );
		}

		// No such constructor

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"java.lang.String\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), TestInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "class org.metawidget.config.TestInspector does not have a constructor that takes a class java.lang.String, as specified by your config attribute".equals( e.getMessage() ) );
		}

		// Different constructor

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<xmlInspector xmlns=\"java:org.metawidget.inspector.xml\" config=\"java.lang.String\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), XmlInspector.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "class org.metawidget.inspector.xml.XmlInspector does not have a constructor that takes a class java.lang.String, as specified by your config attribute. Did you mean config=\"XmlInspectorConfig\"?".equals( e.getMessage() ) );
		}

		// Config-less constructor

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<object xmlns=\"java:java.lang\" config=\"java.lang.String\"/>";
		xml += "</metawidget>";

		try {
			new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), Object.class );
			assertTrue( false );
		} catch ( MetawidgetException e ) {
			assertTrue( "class java.lang.Object does not have a constructor that takes a class java.lang.String, as specified by your config attribute. It only has a config-less constructor".equals( e.getMessage() ) );
		}

		// Superclass does, but subclass doesn't, but no methods

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestNoEqualsSubclassInspectorConfig\"/>";
		xml += "</metawidget>";

		LogUtilsTest.clearLastWarnMessage();

		new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );

		assertTrue( null == LogUtilsTest.getLastWarnMessage() );

		// Superclass does, but subclass doesn't, and has methods

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestNoEqualsHasMethodsSubclassInspectorConfig\"/>";
		xml += "</metawidget>";

		new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );

		assertEquals( "class org.metawidget.config.TestNoEqualsHasMethodsSubclassInspectorConfig does not override .equals() (only its superclass org.metawidget.config.TestInspectorConfig does), so may not be cached reliably", LogUtilsTest.getLastWarnMessage() );

		// Overridden, but uses super.hashCode

		xml = "<?xml version=\"1.0\"?>";
		xml += "<metawidget xmlns=\"http://metawidget.org\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://metawidget.org http://metawidget.org/xsd/metawidget-1.0.xsd\" version=\"1.0\">";
		xml += "<testInspector xmlns=\"java:org.metawidget.config\" config=\"TestDumbHashCodeInspectorConfig\"/>";
		xml += "</metawidget>";

		new ConfigReader().configure( new ByteArrayInputStream( xml.getBytes() ), Inspector.class );

		assertTrue( "class org.metawidget.config.TestDumbHashCodeInspectorConfig overrides .hashCode(), but it returns the same as System.identityHashCode, so cannot be cached reliably".equals( LogUtilsTest.getLastWarnMessage() ) );
	}

	public void testLookupClass()
		throws Exception {

		assertEquals( new ConfigReader().lookupClass( "java:" + ConfigReaderTest.class.getPackage().getName(), ConfigReaderTest.class.getSimpleName() ), ConfigReaderTest.class );
		assertEquals( new ConfigReader().lookupClass( "java:" + ConfigReaderTest.class.getName(), Foo.class.getSimpleName() ), Foo.class );
	}

	//
	// Inner class
	//

	static class Foo {

		// Just an inner class
	}
}