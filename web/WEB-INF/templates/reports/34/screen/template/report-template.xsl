<?xml version="1.0" encoding="us-ascii"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="US-ASCII"
		doctype-public="-//W3C//DTD HTML 4.01//EN"
		doctype-system="http://www.w3.org/TR/html4/strict.dtd" indent="yes" />

	<xsl:template match="/">
		<html>
			<head>
				<link rel="stylesheet" type="text/css"
					href="{/source/request/@context-path}/orgs/{/source/org/@id}/reports/9/stream/output/" />

				<title>
					<xsl:value-of select="/source/org/@name" />
					&gt; DSOs &gt;
					<xsl:value-of
						select="/source/rate-script/dso-service/dso/@code" />
					&gt; Services &gt;
					<xsl:value-of
						select="/source/rate-script/dso-service/@name" />
					&gt; Rate Script
					<xsl:value-of select="/source/rate-script/@id" />
				</title>
			</head>
			<body>
				<p>
					<a
						href="{/source/request/@context-path}/orgs/{/source/org/@id}/reports/0/screen/output/">
						<xsl:value-of select="/source/org/@name" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/orgs/{/source/org/@id}/reports/68/screen/output/">
						<xsl:value-of select="'DSOs'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/orgs/{/source/org/@id}/reports/69/screen/output/?dso-id={/source/rate-script/dso-service/dso/@id}">
						<xsl:value-of
							select="/source/rate-script/dso-service/dso/@code" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/orgs/{/source/org/@id}/reports/32/screen/output/?dso-id={/source/rate-script/dso-service/dso/@id}">
						<xsl:value-of select="'Services'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/orgs/{/source/org/@id}/reports/33/screen/output/?dso-service-id={/source/rate-script/dso-service/@id}">
						<xsl:value-of
							select="/source/rate-script/dso-service/@name" />
					</a>
					&gt; Rate Script
					<xsl:value-of select="/source/rate-script/@id" />
				</p>
				<br />
				<xsl:if test="//message">
					<ul>
						<xsl:for-each select="//message">
							<li>
								<xsl:value-of select="@description" />
							</li>
						</xsl:for-each>
					</ul>
				</xsl:if>
				<table>
					<caption>Properties</caption>
					<thead>
						<tr>
							<th>Name</th>
							<th>Value</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>Id</td>
							<td>
								<xsl:value-of
									select="/source/rate-script/@id" />
							</td>
						</tr>
						<tr>
							<td>Start Date</td>
							<td>
								<xsl:value-of
									select="concat(/source/rate-script/hh-end-date[@label='start']/@year, '-', /source/rate-script/hh-end-date[@label='start']/@month, '-', /source/rate-script/hh-end-date[@label='start']/@day)" />
							</td>
						</tr>
						<tr>
							<td>Finish Date</td>
							<td>
								<xsl:choose>
									<xsl:when
										test="/source/rate-script/hh-end-date[@label='finish']">
										<xsl:value-of
											select="concat(/source/rate-script/hh-end-date[@label='finish']/@year, '-', /source/rate-script/hh-end-date[@label='finish']/@month, '-', /source/rate-script/hh-end-date[@label='finish']/@day)" />
									</xsl:when>
									<xsl:otherwise>
										Ongoing
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
					</tbody>
				</table>

				<h2>Script</h2>

				<pre>
					<xsl:value-of select="/source/rate-script/@script" />
				</pre>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
