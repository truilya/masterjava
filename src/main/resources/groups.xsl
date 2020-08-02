<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:param name="project_name"/>
    <xsl:strip-space elements="*"/>
    <xsl:variable name="path" select="/*[name()='Payload']/*[name()='Projects']/*[name()='Project'][@name=$project_name]/*[name()='Groups']/*[name()='Group']"/>
    <xsl:template match="/">
        <html>
            <body>
                <h1>Группы проекта</h1>
                <table border="1">
                    <thead>
                        <tr>
                            <td width="120">Group name</td>
                            <td width="80">Type</td>
                            <td width="300">Users</td>
                        </tr>
                    </thead>
                    <th>
                        <xsl:apply-templates
                                select="$path"/>
                    </th>

                </table>
            </body>
        </html>
    </xsl:template>
    <xsl:template
            match="/*[name()='Payload']/*[name()='Projects']/*[name()='Project'][@name=$project_name]/*[name()='Groups']/*[name()='Group']">
        <tr>
            <td>
                <xsl:value-of select="@name"/>
            </td>
            <td>
                <xsl:value-of select="@type"/>
            </td>
            <td>
                <xsl:value-of select="$path/*[name()='Users']"/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="text()"/>

</xsl:stylesheet>