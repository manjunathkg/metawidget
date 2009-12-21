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

package org.metawidget.jsp.tagext.html.layout;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.util.Map;

import javax.servlet.jsp.tagext.Tag;

import org.metawidget.jsp.tagext.LiteralTag;
import org.metawidget.jsp.tagext.MetawidgetTag;
import org.metawidget.jsp.tagext.layout.JspFlatSectionLayoutDecorator;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.simple.StringUtils;

/**
 * Layout to decorate widgets from different sections using a DIV.
 *
 * @author Richard Kennard
 */

public class DivLayoutDecorator
	extends JspFlatSectionLayoutDecorator
{
	//
	// Private members
	//

	private String	mStyle;

	private String	mStyleClass;

	//
	// Constructor
	//

	public DivLayoutDecorator( DivLayoutDecoratorConfig config )
	{
		super( config );

		mStyle = config.getStyle();
		mStyleClass = config.getStyleClass();
	}

	//
	// Protected methods
	//

	@Override
	protected void addSectionWidget( String section, Tag containerTag, MetawidgetTag metawidgetTag )
	{
		StringBuffer buffer = new StringBuffer( "<div" );

		if ( mStyle != null )
		{
			buffer.append( " style=\"" );
			buffer.append( mStyle );
			buffer.append( "\"" );
		}

		if ( mStyleClass != null )
		{
			buffer.append( " class=\"" );
			buffer.append( mStyleClass );
			buffer.append( "\"" );
		}

		buffer.append( ">" );

		// Section name (possibly localized)

		String localizedSection = metawidgetTag.getLocalizedKey( StringUtils.camelCase( section ) );

		if ( localizedSection != null )
			buffer.append( localizedSection );
		else
			buffer.append( section );

		buffer.append( "</div>" );

		Map<String, String> attributes = CollectionUtils.newHashMap();
		attributes.put( LABEL, "" );
		attributes.put( WIDE, TRUE );

		getDelegate().layoutWidget( new LiteralTag( buffer.toString() ), PROPERTY, attributes, containerTag, metawidgetTag );
	}
}
