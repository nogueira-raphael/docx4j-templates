<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">

    <xsl:output method="xml" indent="yes"/>

    <!-- Copia tudo por padrão -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Seleciona células onde o texto convertido para número é > 50 -->
    <xsl:template match="w:tc[
        number(
            translate(
                normalize-space(string(.)),
                ' $€RrSs,',
                ''
            )
        ) > 50
    ]">

        <xsl:copy>
            <!-- copia atributos -->
            <xsl:apply-templates select="@*"/>

            <!-- preserva ou cria tcPr -->
            <xsl:choose>
                <xsl:when test="w:tcPr">
                    <xsl:copy-of select="w:tcPr"/>
                </xsl:when>
                <xsl:otherwise>
                    <w:tcPr/>
                </xsl:otherwise>
            </xsl:choose>

            <!-- adiciona a sombra -->
            <w:tcPr>
                <w:shd w:val="clear" w:color="auto" w:fill="FF9999"/>
            </w:tcPr>

            <!-- copia conteúdo restante -->
            <xsl:apply-templates select="node()[not(self::w:tcPr)]"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
