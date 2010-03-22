<?xml version="1.0" encoding="us-ascii"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="US-ASCII"
		doctype-public="-//W3C//DTD HTML 4.01//EN" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
		indent="yes" />
	<xsl:template match="/">
		<html>
			<head>
				<link rel="stylesheet" type="text/css"
					href="{/source/request/@context-path}/style/" />
				<title>
					Chellow &gt; Supplier Contracts &gt;
					<xsl:value-of
						select="/source/bill-snag/bill/batch/supplier-contract/@name" />
					&gt; Batches &gt;
					<xsl:value-of select="/source/bill-snag/bill/batch/@id" />
					&gt; Bills &gt;
					<xsl:value-of select="/source/bill-snag/bill/@id" />
					&gt; Snags &gt;
					<xsl:value-of select="/source/bill-snag/@id" />
				</title>
			</head>
			<body>
				<xsl:if test="//message">
					<ul>
						<xsl:for-each select="//message">
							<li>
								<xsl:value-of select="@description" />
							</li>
						</xsl:for-each>
					</ul>

				</xsl:if>
				<p>
					<a href="{/source/request/@context-path}/">
						<img src="{/source/request/@context-path}/logo/" />
						<span class="logo">Chellow</span>
					</a>
					&gt;
					<a href="{/source/request/@context-path}/supplier-contracts/">
						<xsl:value-of select="'Supplier Contracts'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/bill-snag/bill/batch/supplier-contract/@id}/">
						<xsl:value-of
							select="/source/bill-snag/bill/batch/supplier-contract/@name" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/bill-snag/bill/batch/supplier-contract/@id}/batches/">
						<xsl:value-of select="'Batches'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/bill-snag/bill/batch/supplier-contract/@id}/batches/{/source/bill-snag/bill/batch/@id}/">
						<xsl:value-of select="/source/bill-snag/bill/batch/@reference" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/bill-snag/bill/batch/supplier-contract/@id}/batches/{/source/bill-snag/bill/batch/@id}/bills/">
						<xsl:value-of select="'Bills'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/bill-snag/bill/batch/supplier-contract/@id}/batches/{/source/bill-snag/bill/batch/@id}/bills/{/source/bill-snag/bill/@id}/">
						<xsl:value-of select="/source/bill-snag/bill/@id" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/bill-snag/bill/batch/supplier-contract/@id}/batches/{/source/bill-snag/bill/batch/@id}/bills/{/source/bill-snag/bill/@id}/snags/">
						<xsl:value-of select="'Snags'" />
					</a>
					&gt;
					<xsl:value-of select="concat(/source/bill-snag/@id, ' [')" />
					<a
						href="{/source/request/@context-path}/orgs/{/source/bill-snag/supplier-contract/org/@id}/reports/53/screen/output/?snag-id={/source/bill-snag/@id}">
						<xsl:value-of select="'view'" />
					</a>
					<xsl:value-of select="']'" />
				</p>
				<br />

				<table>
					<tr>
						<th>Chellow Id</th>
						<td>
							<xsl:value-of select="/source/bill-snag/@id" />
						</td>
					</tr>
					<tr>

						<th>Date Created</th>
						<td>
							<xsl:value-of
								select="concat(/source/bill-snag/date[@label='created']/@year, '-', /source/bill-snag/date[@label='created']/@month, '-', /source/bill-snag/date[@label='created']/@day)" />
						</td>

					</tr>
					<tr>
						<th>Is Ignored?</th>
						<td>
							<xsl:choose>
								<xsl:when test="/source/bill-snag/@is-ignored = 'true'">
									Yes
								</xsl:when>

								<xsl:otherwise>
									No
								</xsl:otherwise>
							</xsl:choose>
						</td>
					</tr>
					<tr>
						<th>Description</th>
						<td>
							<xsl:value-of select="/source/bill-snag/@description" />

						</td>
					</tr>
				</table>
				<br />
				<form action="." method="post">
					<fieldset>
						<legend>Ignore Bill Snag</legend>
						<input type="submit" value="Ignore" />

					</fieldset>
				</form>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>