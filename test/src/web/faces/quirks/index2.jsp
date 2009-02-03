<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://metawidget.org/faces" prefix="m" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<body>

		<f:view>
		
			<h1>Quirks</h1>

			<m:metawidget value="#{quirks}">
				<f:param name="columns" value="0"/>
				<m:stub value="#{quirks.refresh}"/>
			</m:metawidget>

			<h1>Quirks (Again)</h1>

			<m:metawidget value="#{quirks}">
				<m:stub value="#{quirks.refresh}"/>
			</m:metawidget>

			<h1>Quirks (Yet Again)</h1>

			<m:metawidget value="#{quirks}">
				<m:metawidget value="#{quirks.boolean}" rendererType="simple"/>
				<h:outputText value="#{quirks.large}"/>
				<m:metawidget value="#{quirks.strings}"/>
				<m:stub value="#{quirks.refresh}"/>
			</m:metawidget>

		</f:view>
		
	</body>	    
</html>
