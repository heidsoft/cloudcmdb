<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.cmdbuild.services.SessionVars" %>
<%@ page import="org.cmdbuild.services.gis.GisDatabaseService" %>
<%@ page import="org.cmdbuild.spring.SpringIntegrationUtils" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>

<%
	final String lang = SpringIntegrationUtils.applicationContext().getBean(SessionVars.class).getLanguage();
	final String jdbcDriverVersion = GisDatabaseService.getDriverVersion();
	final String extVersion = "4.2.0";
%>

<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all.css" />
		<link rel="icon" type="image/x-icon" href="images/favicon.ico" />

		<!-- 0. ExtJS -->
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/ext-all.js"></script>
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>-ux/Notification.js"></script>

		<!-- 1. Main script -->
		<script type="text/javascript" src="javascripts/cmdbuild/core/LoaderConfig.js"></script>
		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Message.js"></script>

		<!-- 2. Localizations -->
		<%@ include file="localizationsJsFiles.jsp" %>

		<!-- 3. Runtime configuration -->
		<script type="text/javascript">
			Ext.ns('CMDBuild.configuration.runtime');
			CMDBuild.configuration.runtime = Ext.create('CMDBuild.model.configuration.Configure');
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.JDBC_DRIVER_VERSION, '<%= StringEscapeUtils.escapeEcmaScript(jdbcDriverVersion) %>');
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.LANGUAGE, '<%= StringEscapeUtils.escapeEcmaScript(lang) %>');
		</script>

		<script type="text/javascript" src="javascripts/cmdbuild/override/form/field/VTypes.js"></script>

		<!-- 4. Modules -->
		<script type="text/javascript" src="javascripts/cmdbuild/Configure.js"></script>

		<title>CMDBuild - Configuration</title>
	</head>
	<body>
		<div id="header">
			<a href="http://www.cmdbuild.org" target="_blank"><img src="images/logo.jpg" alt="CMDBuild logo" /></a>
			<div class="description">Open Source Configuration and Management Database</div>
		</div>

		<div id="footer">
			<div class="left"><a href="http://www.cmdbuild.org" target="_blank">www.cmdbuild.org</a></div>
			<div id="cmdbuild-credits-link" class="center"><tr:translation key="common.credits"/></div>
			<div class="right"><a href="http://www.tecnoteca.com" target="_blank">Copyright &copy; Tecnoteca srl</a></div>
		</div>
	</body>
</html>