<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="true" %>
<%@ page import="org.cmdbuild.services.SessionVars" %>
<%@ page import="org.cmdbuild.spring.SpringIntegrationUtils" %>
<%@ page import="org.cmdbuild.filters.AuthFilter" %>
<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%
	final String lang = SpringIntegrationUtils.applicationContext().getBean(SessionVars.class).getLanguage();
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
		<script type="text/javascript" src="javascripts/cmdbuild/core/constants/Proxy.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/LoaderConfig.js"></script>
		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Message.js"></script>

		<!-- 2. Localizations -->
		<%@ include file="localizationsJsFiles.jsp" %>

		<!-- 3. Runtime configuration -->
		<script type="text/javascript">
			Ext.ns('CMDBuild.configuration.runtime'); // Runtime configurations
			CMDBuild.configuration.runtime = Ext.create('CMDBuild.model.configuration.Runtime');
		</script>

		<!-- 4. Logout script -->
		<script type="text/javascript" src="javascripts/cmdbuild/Logout.js"></script>

		<title>CMDBuild</title>
	</head>
	<body></body>
</html>