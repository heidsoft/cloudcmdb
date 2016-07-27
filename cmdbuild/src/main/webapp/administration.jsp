<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@page import="org.cmdbuild.logic.auth.StandardSessionLogic"%>
<%@page import="org.cmdbuild.logic.auth.SessionLogic"%>
<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.Collection" %>
<%@ page import="com.google.common.base.Joiner" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>
<%@ page import="org.cmdbuild.auth.UserStore" %>
<%@ page import="org.cmdbuild.auth.acl.CMGroup" %>
<%@ page import="org.cmdbuild.auth.user.OperationUser" %>
<%@ page import="org.cmdbuild.services.SessionVars" %>
<%@ page import="org.cmdbuild.spring.SpringIntegrationUtils" %>

<%
	final SessionLogic sessionLogic = SpringIntegrationUtils.applicationContext().getBean(StandardSessionLogic.class);
	final String lang = SpringIntegrationUtils.applicationContext().getBean(SessionVars.class).getLanguage();
	final UserStore userStore = SpringIntegrationUtils.applicationContext().getBean(UserStore.class);
	final OperationUser operationUser = userStore.getUser();
	final CMGroup group = operationUser.getPreferredGroup();
	final String defaultGroupName = operationUser.getAuthenticatedUser().getDefaultGroupName();
	final Collection<String> groupDescriptionList = operationUser.getAuthenticatedUser().getGroupDescriptions();
	final String groupDecriptions = Joiner.on(", ").join(groupDescriptionList);
	final String extVersion = "4.2.0";

	if (!operationUser.hasAdministratorPrivileges()) {
		response.sendRedirect("management.jsp");
	}
%>

<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all-gray.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>-ux/css/MultiSelect.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>-ux/css/portal.css" />
		<link rel="icon" type="image/x-icon" href="images/favicon.ico" />

		<%@ include file="libsJsFiles.jsp"%>

		<!-- 1. Main script -->
		<script type="text/javascript" src="javascripts/cmdbuild/core/constants/Proxy.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/LoaderConfig.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Utils.js"></script>
		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/constants/FieldWidths.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/interfaces/Ajax.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Message.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/CookiesManager.js"></script>

		<!-- 2. Localizations -->
		<%@ include file="localizationsJsFiles.jsp" %>

		<!-- 3. Runtime configuration -->
		<script type="text/javascript">
			CMDBuild.core.CookiesManager.authorizationSet('<%= sessionLogic.getCurrent() %>'); // Authorization cookie setup

			Ext.ns('CMDBuild.configuration.runtime');
			CMDBuild.configuration.runtime = Ext.create('CMDBuild.model.configuration.Runtime');
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE, <%= operationUser.getAuthenticatedUser().canChangePassword() %>);
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_DESCRIPTION, '<%= StringEscapeUtils.escapeEcmaScript(group.getDescription()) %>');
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_ID, <%= group.getId() %>);
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME, '<%= StringEscapeUtils.escapeEcmaScript(group.getName()) %>');
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR, true);
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.LANGUAGE, '<%= StringEscapeUtils.escapeEcmaScript(lang) %>');
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.USER_ID, <%= operationUser.getAuthenticatedUser().getId() %>);
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.USERNAME, '<%= StringEscapeUtils.escapeEcmaScript(operationUser.getAuthenticatedUser().getUsername()) %>');
		</script>

		<%@ include file="coreJsFiles.jsp" %>
		<%@ include file="administrationJsFiles.jsp" %>
<!--
		<script type="text/javascript" src="javascripts/cmdbuild/cmdbuild-core.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/cmdbuild-administration.js"></script>
-->

		<!-- 4. Modules -->
		<script type="text/javascript" src="javascripts/cmdbuild/Administration.js"></script>

		<title>CMDBuild</title>
	</head>
	<body class="cmdb-administration-body">
		<div id="header" class="display-none">
			<a href="http://www.cmdbuild.org" target="_blank"><img src="images/logo.jpg" alt="CMDBuild logo" /></a>
			<div id="instance-name"></div>
			<div class="description">Open Source Configuration and Management Database</div>
			<div id="msg-ct" class="msg-gray">
				<div id="msg">
					<div id="msg-inner">
						<p><tr:translation key="common.user"/>: <strong><%= operationUser.getAuthenticatedUser().getDescription() %></strong> | <a href="logout.jsp"><tr:translation key="common.logout"/></a></p>
						<% if (defaultGroupName == null || "".equals(defaultGroupName) ) { %>
							<p id="msg-inner-hidden"><tr:translation key="common.group"/>: <strong><%= group.getDescription() %></strong>
						<% } else { %>
							<p id="msg-inner-hidden"><tr:translation key="common.group"/>: <strong><tr:translation key="multiGroup"/></strong>
							<script type="text/javascript">
								CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.GROUP_DESCRIPTIONS, '<%= StringEscapeUtils.escapeEcmaScript(groupDecriptions) %>');
							</script>
						<% } %>
							| <a href="management.jsp"><tr:translation key="management.description"/></a>
						</p>
					</div>
				</div>
			</div>
		</div>

		<div id="footer" class="display-none">
			<div class="left"><a href="http://www.cmdbuild.org" target="_blank">www.cmdbuild.org</a></div>
			<div id="cmdbuild-credits-link" class="center"><tr:translation key="common.credits"/></div>
			<div class="right"><a href="http://www.tecnoteca.com" target="_blank">Copyright &copy; Tecnoteca srl</a></div>
		</div>
	</body>
</html>