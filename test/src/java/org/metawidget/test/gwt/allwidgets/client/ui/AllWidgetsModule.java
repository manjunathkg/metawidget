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

package org.metawidget.test.gwt.allwidgets.client.ui;

import java.util.Date;

import org.metawidget.gwt.client.propertybinding.simple.SimpleBinding;
import org.metawidget.gwt.client.propertybinding.simple.SimpleBindingAdapter;
import org.metawidget.gwt.client.ui.Facet;
import org.metawidget.gwt.client.ui.GwtMetawidget;
import org.metawidget.gwt.client.ui.Stub;
import org.metawidget.inspector.gwt.remote.client.GwtRemoteInspectorProxy;
import org.metawidget.test.gwt.allwidgets.client.converter.DateConverter;
import org.metawidget.test.gwt.allwidgets.client.converter.NestedWidgetsConverter;
import org.metawidget.test.shared.allwidgets.model.AllWidgets;
import org.metawidget.test.shared.allwidgets.model.AllWidgets.NestedWidgets;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;

/**
 * @author Richard Kennard
 */

public class AllWidgetsModule
	implements EntryPoint
{
	//
	// Private members
	//

	private Panel	mPanel;

	//
	// Constructor
	//

	public AllWidgetsModule( Panel panel )
	{
		mPanel = panel;
	}

	//
	// Public methods
	//

	public void onModuleLoad()
	{
		// Metawidget

		final GwtMetawidget metawidget = new GwtMetawidget();
		metawidget.setParameter( "sectionStyleName", "aSectionStyleName" );
		metawidget.setParameter( "numberOfColumns", 2 );
		metawidget.setInspector( new GwtRemoteInspectorProxy( "/metawidget-inspector-allwidgets" ) );
		metawidget.setToInspect( new AllWidgets() );

		// PropertyBinding

		metawidget.setPropertyBindingClass( SimpleBinding.class );

		@SuppressWarnings( "unchecked" )
		SimpleBindingAdapter<AllWidgets> allWidgetsAdapter = (SimpleBindingAdapter<AllWidgets>) GWT.create( AllWidgets.class );
		SimpleBinding.registerAdapter( AllWidgets.class, allWidgetsAdapter );
		SimpleBinding.registerConverter( Date.class, new DateConverter() );
		SimpleBinding.registerConverter( NestedWidgets.class, new NestedWidgetsConverter() );

		// ActionBinding

		metawidget.setActionBindingClass( SimpleBinding.class );

		// Stubs

		metawidget.add( new Stub( "mystery" ));

		// Embedded buttons

		Facet buttonsFacet = new Facet();
		buttonsFacet.setName( "buttons" );
		metawidget.add( buttonsFacet );

		final Button saveButton = new Button( "Save" );
		saveButton.addClickHandler( new ClickHandler()
		{
			public void onClick( ClickEvent event )
			{
				metawidget.save();
				metawidget.setReadOnly( true );
				metawidget.setParameter( "numberOfColumns", 0 );
				metawidget.setDictionaryName( "bundle" );
			}
		} );

		buttonsFacet.add( saveButton );

		// Add to the given Panel (for unit tests)

		mPanel.add( metawidget );
	}
}
