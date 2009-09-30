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

package org.metawidget.inspector.gwt.remote.server;

import javax.servlet.ServletConfig;

import org.metawidget.config.ConfigReader;
import org.metawidget.inspector.iface.Inspector;

/**
 * Version of <code>GwtRemoteInspectorTestImpl</code> for running unit tests.
 * <p>
 * This code can be removed once GWT's &lt;servlet&gt; module element supports &lt;init-param&gt;
 * (see http://code.google.com/p/google-web-toolkit/issues/detail?id=4079)
 *
 * @author Richard Kennard
 */

public class GwtRemoteInspectorTestImpl
	extends GwtRemoteInspectorImpl
{
	//
	// Protected methods
	//

	@Override
	protected void configure( ServletConfig servletConfig )
	{
		setInspector( new ConfigReader().configure( "metawidget.xml", Inspector.class ));
	}
}