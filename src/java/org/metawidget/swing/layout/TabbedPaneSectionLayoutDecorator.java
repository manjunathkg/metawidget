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

package org.metawidget.swing.layout;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import org.metawidget.layout.decorator.LayoutDecorator;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.LayoutUtils;
import org.metawidget.util.simple.StringUtils;

/**
 * Layout to decorate widgets from different sections using a JTabbedPane.
 *
 * @author Richard Kennard
 */

public class TabbedPaneSectionLayoutDecorator
	extends LayoutDecorator<JComponent, SwingMetawidget>
{
	//
	// Private statics
	//

	/**
	 * The border around the entire tabbed pane.
	 */

	private final static Border	TABBED_PANE_BORDER	= BorderFactory.createEmptyBorder( 5, 0, 5, 0 );

	/**
	 * The insets around each tab.
	 */

	private final static Border	TAB_PANEL_BORDER	= BorderFactory.createEmptyBorder( 3, 3, 3, 3 );

	//
	// Private members
	//

	private int					mTabPlacement;

	//
	// Constructor
	//

	public TabbedPaneSectionLayoutDecorator( TabbedPaneSectionLayoutDecoratorConfig config )
	{
		super( config );

		mTabPlacement = config.getTabPlacement();
	}

	//
	// Public methods
	//

	@Override
	public void startLayout( JComponent container, SwingMetawidget metawidget )
	{
		super.startLayout( container, metawidget );
		container.putClientProperty( TabbedPaneSectionLayoutDecorator.class, null );
	}

	@Override
	public void layoutWidget( JComponent component, String elementName, Map<String, String> attributes, JComponent container, SwingMetawidget metawidget )
	{
		String section = LayoutUtils.stripSection( attributes );
		State state = getState( container );

		// Stay where we are?

		if ( section == null || section.equals( state.currentSection ) )
		{
			if ( state.tabPanel == null )
				super.layoutWidget( component, elementName, attributes, container, metawidget );
			else
				super.layoutWidget( component, elementName, attributes, state.tabPanel, metawidget );

			return;
		}

		state.currentSection = section;

		// End current section

		JTabbedPane tabbedPane = null;

		if ( state.tabPanel != null )
		{
			super.endLayout( state.tabPanel, metawidget );
			tabbedPane = (JTabbedPane) state.tabPanel.getParent();
			state.tabPanel = null;
		}

		// No new section?

		if ( "".equals( section ) )
		{
			super.layoutWidget( component, elementName, attributes, container, metawidget );
			return;
		}

		// Whole new tabbed pane?

		if ( tabbedPane == null )
		{
			tabbedPane = new JTabbedPane();
			tabbedPane.setBorder( TABBED_PANE_BORDER );
			tabbedPane.setTabPlacement( mTabPlacement );

			Map<String, String> tabbedPaneAttributes = CollectionUtils.newHashMap();
			tabbedPaneAttributes.put( LABEL, "" );
			tabbedPaneAttributes.put( LARGE, TRUE );
			super.layoutWidget( tabbedPane, PROPERTY, tabbedPaneAttributes, container, metawidget );
		}

		// New tab

		state.tabPanel = new JPanel();
		state.tabPanel.setBorder( TAB_PANEL_BORDER );
		super.startLayout( state.tabPanel, metawidget );

		// Tab name (possibly localized)

		String localizedSection = metawidget.getLocalizedKey( StringUtils.camelCase( section ) );

		if ( localizedSection == null )
			localizedSection = section;

		tabbedPane.addTab( localizedSection, state.tabPanel );

		// Add component to new tab

		super.layoutWidget( component, elementName, attributes, state.tabPanel, metawidget );
	}

	@Override
	public void endLayout( JComponent container, SwingMetawidget metawidget )
	{
		State state = getState( container );

		if ( state.tabPanel != null )
			super.endLayout( state.tabPanel, metawidget );

		super.endLayout( container, metawidget );
	}

	//
	// Private methods
	//

	private State getState( JComponent container )
	{
		State state = (State) container.getClientProperty( TabbedPaneSectionLayoutDecorator.class );

		if ( state == null )
		{
			state = new State();
			container.putClientProperty( TabbedPaneSectionLayoutDecorator.class, state );
		}

		return state;
	}

	//
	// Inner class
	//

	/**
	 * Simple, lightweight structure for saving state.
	 */

	/* package private */class State
	{
		/* package private */String	currentSection;

		/* package private */JPanel	tabPanel;
	}
}