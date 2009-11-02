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

package org.metawidget.inspector.annotation;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.util.Map;

import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.BaseObjectInspectorConfig;
import org.metawidget.inspector.impl.actionstyle.Action;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.util.ArrayUtils;
import org.metawidget.util.CollectionUtils;

/**
 * Inspects annotations defined by Metawidget (declared in this same package).
 * <p>
 * Note: the name of this class is longwinded for extra clarity. It is not just a
 * 'MetawidgetInspector', because of course there are lots of different Metawidget Inspectors.
 * Equally, it is not just an 'AnnotationInspector', because it doesn't generically scan all
 * possible annotations.
 *
 * @author Richard Kennard
 */

public class MetawidgetAnnotationInspector
	extends BaseObjectInspector
{
	//
	// Constructor
	//

	public MetawidgetAnnotationInspector()
	{
		this( new BaseObjectInspectorConfig() );
	}

	public MetawidgetAnnotationInspector( BaseObjectInspectorConfig config )
	{
		super( config );
	}

	//
	// Protected methods
	//

	@Override
	protected Map<String, String> inspectProperty( Property property )
		throws Exception
	{
		Map<String, String> attributes = CollectionUtils.newHashMap();

		// UiRequired

		if ( property.isAnnotationPresent( UiRequired.class ) )
			attributes.put( REQUIRED, TRUE );

		// UiLookup

		UiLookup lookup = property.getAnnotation( UiLookup.class );

		if ( lookup != null )
		{
			attributes.put( LOOKUP, ArrayUtils.toString( lookup.value() ) );

			// (note: values().length == labels().length() is not validated
			// here, as XmlInspector could bypass it anyway)

			if ( lookup.labels().length > 0 )
				attributes.put( LOOKUP_LABELS, ArrayUtils.toString( lookup.labels() ) );
		}

		// UiMasked

		if ( property.isAnnotationPresent( UiMasked.class ) )
			attributes.put( MASKED, TRUE );

		// UiHidden

		if ( property.isAnnotationPresent( UiHidden.class ) )
			attributes.put( HIDDEN, TRUE );

		// UiLarge

		if ( property.isAnnotationPresent( UiLarge.class ) )
			attributes.put( LARGE, TRUE );

		// UiWide

		if ( property.isAnnotationPresent( UiWide.class ) )
			attributes.put( WIDE, TRUE );

		// UiComesAfter

		UiComesAfter comesAfter = property.getAnnotation( UiComesAfter.class );

		if ( comesAfter != null )
			attributes.put( COMES_AFTER, ArrayUtils.toString( comesAfter.value() ));

		// UiReadOnly

		UiReadOnly readOnly = property.getAnnotation( UiReadOnly.class );

		if ( readOnly != null )
			attributes.put( READ_ONLY, TRUE );

		// UiDontExpand

		UiDontExpand dontExpand = property.getAnnotation( UiDontExpand.class );

		if ( dontExpand != null )
			attributes.put( DONT_EXPAND, TRUE );

		// UiSection

		UiSection uiSection = property.getAnnotation( UiSection.class );

		if ( uiSection != null )
			attributes.put( SECTION, uiSection.value() );

		// UiLabel

		UiLabel label = property.getAnnotation( UiLabel.class );

		if ( label != null )
			attributes.put( LABEL, label.value() );

		// UiAttribute

		UiAttribute attribute = property.getAnnotation( UiAttribute.class );

		if ( attribute != null )
		{
			attributes.put( attribute.name(), attribute.value() );
		}

		// UiAttributes

		UiAttributes uiAttributes = property.getAnnotation( UiAttributes.class );

		if ( uiAttributes != null )
		{
			for ( UiAttribute nestedAttribute : uiAttributes.value() )
			{
				attributes.put( nestedAttribute.name(), nestedAttribute.value() );
			}
		}

		return attributes;
	}

	@Override
	protected Map<String, String> inspectAction( Action action )
		throws Exception
	{
		Map<String, String> attributes = CollectionUtils.newHashMap();

		// UiAction (this is kind of a dummy match)

		if ( action.isAnnotationPresent( UiAction.class ) )
			attributes.put( NAME, action.getName() );

		// UiHidden

		if ( action.isAnnotationPresent( UiHidden.class ) )
			attributes.put( HIDDEN, TRUE );

		// UiComesAfter

		UiComesAfter comesAfter = action.getAnnotation( UiComesAfter.class );

		if ( comesAfter != null )
			attributes.put( COMES_AFTER, ArrayUtils.toString( comesAfter.value() ));

		// UiReadOnly

		UiReadOnly readOnly = action.getAnnotation( UiReadOnly.class );

		if ( readOnly != null )
			attributes.put( READ_ONLY, TRUE );

		// UiSection

		UiSection uiSection = action.getAnnotation( UiSection.class );

		if ( uiSection != null )
			attributes.put( SECTION, uiSection.value() );

		// UiLabel

		UiLabel label = action.getAnnotation( UiLabel.class );

		if ( label != null )
			attributes.put( LABEL, label.value() );

		// UiAttribute

		UiAttribute attribute = action.getAnnotation( UiAttribute.class );

		if ( attribute != null )
		{
			attributes.put( attribute.name(), attribute.value() );
		}

		// UiAttributes

		UiAttributes uiAttributes = action.getAnnotation( UiAttributes.class );

		if ( uiAttributes != null )
		{
			for ( UiAttribute nestedAttribute : uiAttributes.value() )
			{
				attributes.put( nestedAttribute.name(), nestedAttribute.value() );
			}
		}

		return attributes;
	}
}
