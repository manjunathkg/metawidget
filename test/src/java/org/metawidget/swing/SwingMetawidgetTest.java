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

package org.metawidget.swing;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import junit.framework.TestCase;

import org.metawidget.example.shared.addressbook.model.PersonalContact;
import org.metawidget.inspector.annotation.MetawidgetAnnotationInspector;
import org.metawidget.inspector.annotation.UiAction;
import org.metawidget.inspector.annotation.UiRequired;
import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import org.metawidget.inspector.propertytype.PropertyTypeInspectorTest.RecursiveFoo;
import org.metawidget.swing.layout.GridBagLayoutConfig;
import org.metawidget.swing.widgetbuilder.SwingWidgetBuilder;
import org.metawidget.swing.widgetprocessor.binding.beansbinding.BeansBindingProcessor;
import org.metawidget.swing.widgetprocessor.binding.beanutils.BeanUtilsBindingProcessor;
import org.metawidget.util.CollectionUtils;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;

/**
 * @author Richard Kennard
 */

public class SwingMetawidgetTest
	extends TestCase
{
	//
	// Public methods
	//

	public void testBeanInfo()
		throws Exception
	{
		BeanInfo info = Introspector.getBeanInfo( SwingMetawidget.class );

		assertTrue( "Metawidget".equals( info.getBeanDescriptor().getDisplayName() ) );
		assertTrue( info.getIcon( BeanInfo.ICON_MONO_16x16 ) != null );
		assertTrue( info.getIcon( BeanInfo.ICON_COLOR_16x16 ) != null );
		assertTrue( info.getIcon( BeanInfo.ICON_MONO_32x32 ) != null );
		assertTrue( info.getIcon( BeanInfo.ICON_COLOR_32x32 ) != null );
		assertTrue( info.getIcon( 5 ) == null );
	}

	@SuppressWarnings( "serial" )
	public void testRepainting()
	{
		final StringBuilder builder = new StringBuilder();

		SwingMetawidget metawidget = new SwingMetawidget()
		{
			@Override
			public void repaint()
			{
				builder.append( "repaint\n" );
				super.repaint();
			}
		};

		// Will see some 'repaint's because of the JPanel

		String panelRepaints = builder.toString();

		// Should see 'repaint' because of the add

		JTextField component = new JTextField();
		metawidget.add( component );
		assertTrue( ( panelRepaints + "repaint\n" ).equals( builder.toString() ) );

		// Should see another 'repaint' because of remove called .getComponent, which built the
		// widgets

		metawidget.remove( component );
		assertTrue( ( panelRepaints + "repaint\nrepaint\n" ).equals( builder.toString() ) );

		// Should not see another repaint, because widgets already need repainting

		metawidget.add( component );
		assertTrue( ( panelRepaints + "repaint\nrepaint\n" ).equals( builder.toString() ) );

		// Should see another repaint, because .remove will build the widgets

		metawidget.remove( 0 );
		assertTrue( ( panelRepaints + "repaint\nrepaint\nrepaint\n" ).equals( builder.toString() ) );

		// Should not see another repaint, because widgets already need repainting

		metawidget.removeAll();
		assertTrue( ( panelRepaints + "repaint\nrepaint\nrepaint\n" ).equals( builder.toString() ) );
	}

	public void testNestedWithManualInspector()
	{
		SwingMetawidget metawidget = new SwingMetawidget();
		metawidget.setInspector( new PropertyTypeInspector() );
		metawidget.addWidgetProcessor( new BeanUtilsBindingProcessor() );
		Foo foo1 = new Foo();
		Foo foo2 = new Foo();
		foo1.setFoo( foo2 );
		foo2.setName( "Bar" );
		metawidget.setToInspect( foo1 );

		assertTrue( "Bar".equals( metawidget.getValue( "foo", "name" ) ) );
	}

	public void testRecursion()
	{
		SwingMetawidget metawidget = new SwingMetawidget();
		metawidget.setInspector( new PropertyTypeInspector() );
		RecursiveFoo foo = new RecursiveFoo();
		foo.foo = foo;
		metawidget.setToInspect( foo );

		assertTrue( null == ( (SwingMetawidget) metawidget.getComponent( "foo" ) ).getComponent( "foo" ) );
	}

	public void testMaximumInspectionDepth()
	{
		Foo foo1 = new Foo();
		Foo foo2 = new Foo();
		Foo foo3 = new Foo();
		foo1.setFoo( foo2 );
		foo2.setFoo( foo3 );
		foo2.setName( "Bar" );
		foo3.setFoo( new Foo() );

		SwingMetawidget metawidget = new SwingMetawidget();
		metawidget.setInspector( new PropertyTypeInspector() );
		metawidget.setToInspect( foo1 );
		metawidget.setMaximumInspectionDepth( 0 );
		assertTrue( metawidget.getComponent( "foo" ) == null );

		metawidget.setMaximumInspectionDepth( 1 );
		assertTrue( 1 == metawidget.getMaximumInspectionDepth() );
		assertTrue( metawidget.getComponent( "foo" ) instanceof SwingMetawidget );
		assertTrue( metawidget.getComponent( "foo", "name" ) instanceof JTextField );
		assertTrue( "name".equals( metawidget.getComponent( "foo", "name" ).getName() ) );
		assertTrue( metawidget.getComponent( "foo", "foo" ) == null );

		metawidget.setMaximumInspectionDepth( 2 );
		assertTrue( metawidget.getComponent( "foo", "foo" ) instanceof SwingMetawidget );
		assertTrue( metawidget.getComponent( "foo", "foo", "foo" ) == null );

		metawidget.setMaximumInspectionDepth( 3 );
		assertTrue( metawidget.getComponent( "foo", "foo", "foo" ) instanceof SwingMetawidget );
		assertTrue( metawidget.getComponent( "foo", "foo", "foo", "foo" ) == null );

		metawidget.setMaximumInspectionDepth( 4 );

		// Goes: root (foo1) -> foo (foo2) -> foo (foo3) -> foo (new Foo) -> foo == null
		//
		// ...but because we know the type of the child property, we end up putting an empty
		// Metawidget

		assertTrue( metawidget.getComponent( "foo", "foo", "foo", "foo" ) instanceof SwingMetawidget );
		assertTrue( 1 == ( (SwingMetawidget) metawidget.getComponent( "foo", "foo", "foo", "foo" ) ).getComponentCount() );
		assertTrue( metawidget.getComponent( "foo", "foo", "foo", "foo", "foo" ) == null );
	}

	public void testRebind()
		throws Exception
	{
		_testRebind( new BeansBindingProcessor(), org.jdesktop.beansbinding.Binding.SyncFailureType.SOURCE_UNREADABLE.toString() );
		_testRebind( new BeanUtilsBindingProcessor(), "Property 'name' has no getter" );
	}

	public void testGroovy()
		throws Exception
	{
		// Test supporting the enum dropdown for a dynamic type...

		// ...both nullable (the default)...

		Map<String, String> attributes = CollectionUtils.newHashMap();
		attributes.put( TYPE, "dynamic-type-that-cant-be-loaded" );
		attributes.put( LOOKUP, "bar,baz" );

		SwingMetawidget metawidget = new SwingMetawidget();
		SwingWidgetBuilder widgetBuilder = new SwingWidgetBuilder();

		JComponent component = widgetBuilder.buildWidget( "foo", attributes, metawidget );
		assertTrue( component instanceof JComboBox );
		assertTrue( 3 == ( (JComboBox) component ).getItemCount() );

		// ...and not-nullable...

		attributes.put( REQUIRED, TRUE );

		component = widgetBuilder.buildWidget( "foo", attributes, metawidget );
		assertTrue( component instanceof JComboBox );
		assertTrue( 2 == ( (JComboBox) component ).getItemCount() );

		// Also test UiDontExpand on a dynamic type

		attributes.remove( REQUIRED );
		attributes.remove( LOOKUP );

		component = widgetBuilder.buildWidget( "foo", attributes, metawidget );
		assertTrue( null == component );

		attributes.put( DONT_EXPAND, TRUE );

		component = widgetBuilder.buildWidget( "foo", attributes, metawidget );
		assertTrue( component instanceof JTextField );
	}

	public void testNestedActionBinding()
	{
		Foo foo1 = new Foo();
		Foo foo2 = new Foo();
		Foo foo3 = new Foo();
		foo1.setFoo( foo2 );
		foo2.setFoo( foo3 );

		SwingMetawidget metawidget = new SwingMetawidget();
		CompositeInspectorConfig config = new CompositeInspectorConfig();
		config.setInspectors( new MetawidgetAnnotationInspector(), new PropertyTypeInspector() );
		metawidget.setInspector( new CompositeInspector( config ) );
		metawidget.addWidgetProcessor( new FooActionBindingProcessor() );
		metawidget.setToInspect( foo1 );

		( (JButton) metawidget.getComponent( 0 ) ).doClick();
		( (JButton) ( (SwingMetawidget) ( (SwingMetawidget) metawidget.getComponent( 2 ) ).getComponent( 2 ) ).getComponent( 0 ) ).doClick();

		assertTrue( "FooActionBindingProcessor fired".equals( ( (JTextField) metawidget.getComponent( 4 ) ).getText() ) );
		assertTrue( "".equals( ( (JTextField) ( (SwingMetawidget) metawidget.getComponent( 2 ) ).getComponent( 4 ) ).getText() ) );
		assertTrue( "FooActionBindingProcessor fired".equals( ( (JTextField) ( (SwingMetawidget) ( (SwingMetawidget) metawidget.getComponent( 2 ) ).getComponent( 2 ) ).getComponent( 4 ) ).getText() ) );
	}

	public void testFacet()
	{
		SwingMetawidget metawidget = new SwingMetawidget();
		metawidget.add( new Facet() );

		// Without a path, should be no layout

		assertTrue( metawidget.getComponentCount() == 0 );

		// With a path, GridBagLayout spacer panel should appear (but not facet)

		metawidget.setPath( "Foo" );
		assertTrue( metawidget.getComponentCount() == 1 );

		assertTrue( metawidget.getComponent( 0 ) instanceof JPanel );
		assertTrue( metawidget.getComponentCount() == 1 );

		// Normal component should appear (but still not facet)

		metawidget.add( new JTextField() );
		assertTrue( metawidget.getComponent( 0 ) instanceof JTextField );
		assertTrue( metawidget.getComponent( 1 ) instanceof JPanel );
		assertTrue( metawidget.getComponentCount() == 2 );
	}

	public void testRequiredBoolean()
	{
		SwingMetawidget metawidget = new SwingMetawidget();
		metawidget.setToInspect( new FooRequiredBoolean() );

		assertTrue( metawidget.getComponent( 1 ) instanceof JCheckBox );
	}

	public void testLabelString()
	{
		SwingMetawidget metawidget = new SwingMetawidget();

		assertTrue( "".equals( metawidget.getLabelString( null )));

		Map<String, String> attributes = CollectionUtils.newHashMap();
		assertTrue( "".equals( metawidget.getLabelString( attributes )));

		attributes.put( NAME, "foo" );
		assertTrue( "Foo".equals( metawidget.getLabelString( attributes )));

		attributes.put( LABEL, "foo" );
		assertTrue( "foo".equals( metawidget.getLabelString( attributes )));
	}

	public void testValidate()
	{
		final List<String> called = CollectionUtils.newArrayList();

		@SuppressWarnings( "serial" )
		SwingMetawidget metawidget = new SwingMetawidget()
		{
			@Override
			public void validate()
			{
				called.add( "validate" );
			}

			@Override
			protected void endBuild()
			{
				called.add( "endBuild" );
			}
		};

		metawidget.getPipeline().endBuild();

		// validate should be called after super.endBuild in SwingMetawidget

		assertTrue( "endBuild".equals( called.get( 0 )));
		assertTrue( "validate".equals( called.get( 1 )));
		assertTrue( 2 == called.size() );
	}

	public void testGetWidgetProcessor()
	{
		SwingMetawidget metawidget = new SwingMetawidget();
		metawidget.setConfig( "org/metawidget/example/swing/addressbook/metawidget.xml" );
		metawidget.setToInspect( new PersonalContact() );
		assertTrue( null != metawidget.getWidgetProcessor( BeansBindingProcessor.class ));
	}

	//
	// Private methods
	//

	private void _testRebind( WidgetProcessor<JComponent, SwingMetawidget> processor, String errorMessage )
		throws Exception
	{
		// Bind

		Foo foo1 = new Foo();
		foo1.setName( "Charlotte" );
		Foo nestedFoo1 = new Foo();
		foo1.setFoo( nestedFoo1 );
		nestedFoo1.setName( "Philippa" );

		SwingMetawidget metawidget = new SwingMetawidget();
		metawidget.setInspector( new PropertyTypeInspector() );
		metawidget.addWidgetProcessor( processor );
		metawidget.setToInspect( foo1 );

		JTextField textField = metawidget.getComponent( "name" );
		JTextField nestedTextField = metawidget.getComponent( "foo", "name" );
		assertTrue( "Charlotte".equals( textField.getText() ) );
		assertTrue( "Philippa".equals( nestedTextField.getText() ) );

		// Rebind

		Foo foo2 = new Foo();
		foo2.setName( "Julianne" );
		Foo nestedFoo2 = new Foo();
		foo2.setFoo( nestedFoo2 );
		nestedFoo2.setName( "Richard" );

		processor.getClass().getMethod( "rebind", Object.class, SwingMetawidget.class ).invoke( processor, foo2, metawidget );
		assertTrue( "Julianne".equals( textField.getText() ) );
		assertTrue( "Richard".equals( nestedTextField.getText() ) );

		// Check same component

		assertTrue( textField == metawidget.getComponent( "name" ) );
		assertTrue( nestedTextField == metawidget.getComponent( "foo", "name" ) );

		// Check saves back to the correct place

		processor.getClass().getMethod( "save", SwingMetawidget.class ).invoke( processor, metawidget );
		assertTrue( "Charlotte".equals( foo1.getName() ) );
		assertTrue( foo2 == metawidget.getToInspect() );
		assertTrue( "Julianne".equals( foo2.getName() ) );

		// Check different component

		metawidget.setToInspect( foo2 );
		metawidget.setMetawidgetLayout( new org.metawidget.swing.layout.GridBagLayout( new GridBagLayoutConfig().setLabelAlignment( SwingConstants.RIGHT ) ));
		assertTrue( textField != metawidget.getComponent( "name" ) );
		assertTrue( nestedTextField != metawidget.getComponent( "foo", "name" ) );
		assertTrue( SwingConstants.RIGHT == ( (JLabel) metawidget.getComponent( 0 ) ).getHorizontalAlignment() );
		assertTrue( "Julianne".equals( ( (JTextField) metawidget.getComponent( "name" ) ).getText() ) );
		assertTrue( "Richard".equals( ( (JTextField) metawidget.getComponent( "foo", "name" ) ).getText() ) );

		// Check error

		try
		{
			processor.getClass().getMethod( "rebind", Object.class, SwingMetawidget.class ).invoke( processor, new Object(), metawidget );
			assertTrue( false );
		}
		catch ( Exception e )
		{
			assertTrue( errorMessage.equals( e.getCause().getMessage() ) );
		}
	}

	//
	// Inner class
	//

	public static class Foo
	{
		//
		//
		// Private members
		//
		//

		private String	mName;

		private Foo		mFoo;

		//
		//
		// Public methods
		//
		//

		public String getName()
		{
			return mName;
		}

		public void setName( String name )
		{
			mName = name;
		}

		public Foo getFoo()
		{
			return mFoo;
		}

		public void setFoo( Foo foo )
		{
			mFoo = foo;
		}

		@UiAction
		public void doAction()
		{
			// Do nothing
		}
	}

	public static class FooActionBindingProcessor
		implements WidgetProcessor<JComponent, SwingMetawidget>
	{
		//
		// Public methods
		//

		@Override
		@SuppressWarnings( "serial" )
		public JComponent processWidget( JComponent component, String elementName, Map<String, String> attributes, final SwingMetawidget metawidget )
		{
			if ( !ACTION.equals( elementName ) )
				return component;

			JButton button = (JButton) component;

			button.setAction( new AbstractAction( button.getText() )
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					metawidget.setValue( "FooActionBindingProcessor fired", "name" );
				}
			} );

			return component;
		}
	}

	public static class FooRequiredBoolean
	{
		//
		// Public methods
		//

		@UiRequired
		public Boolean getBoolean()
		{
			return null;
		}

		public void setBoolean( Boolean aBoolean )
		{
			// Do nothing
		}
	}
}
