<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/locale/ext-lang-<%= lang %>.js"></script>
<script type="text/javascript" src="services/json/utils/gettranslationobject"></script>

<%
	if ("it".equals(lang)) {
%>
	<script type="text/javascript" src="javascripts/cmdbuild/locale/it.js"></script>
<%
	} else if ("fr".equals(lang)) {
%>
	<script type="text/javascript" src="javascripts/cmdbuild/locale/fr.js"></script>
<%
	} else {
%>
	<script type="text/javascript" src="javascripts/cmdbuild/locale/en.js"></script>
<%
	}
%>