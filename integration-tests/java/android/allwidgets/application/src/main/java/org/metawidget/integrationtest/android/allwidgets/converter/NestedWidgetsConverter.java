// Metawidget (licensed under LGPL)
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

package org.metawidget.integrationtest.android.allwidgets.converter;

import org.metawidget.android.widget.widgetprocessor.binding.simple.Converter;
import org.metawidget.integrationtest.shared.allwidgets.model.AllWidgets.NestedWidgets;
import org.metawidget.util.ArrayUtils;
import org.metawidget.util.simple.StringUtils;

import android.view.View;

/**
 * @author Richard Kennard
 */

public class NestedWidgetsConverter
	implements Converter<NestedWidgets> {

	//
	// Public methods
	//

	public Object convertForView( View widget, NestedWidgets value ) {

		return StringUtils.quietValueOf( value );
	}

	public NestedWidgets convertFromView( View widget, Object value, Class<?> intoClass ) {

		String[] values = ArrayUtils.fromString( (String) value );

		if ( values.length == 0 ) {
			return null;
		}

		NestedWidgets nestedWidgets = new NestedWidgets();
		nestedWidgets.setNestedTextbox1( values[0] );

		if ( values.length > 1 ) {
			nestedWidgets.setNestedTextbox2( values[1] );
		}

		return nestedWidgets;
	}
}
