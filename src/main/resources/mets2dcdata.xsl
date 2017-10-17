<?xml version="1.0" encoding="UTF-8"?>
<stylesheet xmlns:dc="http://purl.org/dc/elements/1.1/"
            xmlns:mets="http://www.loc.gov/METS/"
            xmlns:mods="http://www.loc.gov/mods/v3"
            xmlns:slub="http://slub-dresden.de/"
            xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            version="2.0"
            xmlns="http://www.w3.org/1999/XSL/Transform"
            xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd
    					http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd
                        http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods.xsd
                        http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dc.xsd">

    <output standalone="yes" encoding="utf-8" media-type="application/xml" indent="yes" method="xml"/>
    <strip-space elements="*"/>

    <template match="/mets:mets">
        <oai_dc:dc>
            <apply-templates select="mets:dmdSec[@ID='DMD_000']/mets:mdWrap[@MDTYPE='MODS']/mets:xmlData/mods:mods"/>
            <apply-templates select="mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/slub:info"/>
            <apply-templates select="mets:structMap/mets:div"/>
        </oai_dc:dc>
    </template>

    <template match="mods:mods">
        <apply-templates select="mods:titleInfo[@usage='primary']"/>
        <apply-templates select="mods:identifier[@type='qucosa:urn']"/>
        <apply-templates select="mods:identifier[@type='qucosa:doi']"/>
        <apply-templates select="mods:identifier[@type='isbn']"/>
        <apply-templates select="mods:identifier[@type='issn']"/>
        <apply-templates select="mods:identifier[@type='swb-ppn']"/>
        <apply-templates select="mods:language/mods:languageTerm[@type='code']"/>
        <apply-templates select="mods:relatedItem/mods:identifier"/>
        <apply-templates select="mods:abstract[@type='summary']"/>
        <apply-templates select="mods:classification"/>
        <apply-templates select="mods:name[@type='personal']"/>
        <apply-templates select="mods:name[@type='corporate']"/>
        <apply-templates select="mods:originInfo" />
    </template>

    <template match="slub:info">
        <apply-templates select="slub:documentType"/>
        <apply-templates select="slub:funding/slub:project"/>
    </template>

    <template match="mods:titleInfo">
        <dc:title>
            <value-of select="mods:title"/>
            <variable name="titleInfoLang" select="@lang"/>
            <if test="../mods:titleInfo[@lang=$titleInfoLang]/mods:subTitle">
                <value-of select="concat(':', string-join(../mods:titleInfo[@lang=$titleInfoLang][not(@type='alternative')]/mods:subTitle, ':'))"/>
            </if>
        </dc:title>
    </template>

    <template match="mets:structMap[@TYPE='LOGICAL']/mets:div[1]">
        <dc:type>
            <value-of select="@TYPE"/>
        </dc:type>
    </template>

    <template match="mods:identifier">
        <dc:identifier>
            <value-of select="."/>
        </dc:identifier>
    </template>

    <template match="mods:language/mods:languageTerm[matches(@authority, '^iso639-[1|2]')]">
        <dc:language>
            <value-of select="."/>
        </dc:language>
    </template>

    <template match="mods:relatedItem/mods:identifier">
        <dc:relation>
            <value-of select="."/>
        </dc:relation>
    </template>

    <template match="mods:abstract">
        <dc:description>
            <value-of select="."/>
            <if test="../mods:tableOfContents">
                <value-of select="concat(':', ../mods:tableOfContents)"/>
            </if>
        </dc:description>
    </template>

    <template match="mods:classification[@authority='ddc']">
        <dc:classification>
            <value-of select="concat('info:eu-repo/classification/ddc/', .)"/>
        </dc:classification>
    </template>

    <template match="mods:classification[@authority='z']">
        <dc:classification>
            <value-of select="."/>
        </dc:classification>
    </template>

    <template match="mods:classification[@authority='sswd']">
        <dc:classification>
            <value-of select="replace(., ',', ';')"/>
        </dc:classification>
    </template>

    <template match="mods:name[@type='personal']">
        <variable name="familyName" select="mods:namePart[@type='family']"/>
        <variable name="givenName" select="mods:namePart[@type='given']"/>
        <variable name="code" select="mods:role/mods:roleTerm[@type='code']/text()" />
        
        <choose>
       		<when test="$code = 'aut' or $code = 'cmp'">
       			<dc:creator>
       				<value-of select="if($familyName != '') then concat($familyName, ',', $givenName) else $givenName" />
       			</dc:creator>
       		</when>
       		<when test="$code = 'rev' or $code = 'ctb' or $code = 'ths' or $code = 'sad' or $code = 'pbl' 
       					or $code = 'ill' or $code = 'edt' or $code = 'oth' or $code = 'trl'">
       			<dc:contributor>
       				<value-of select="if($familyName != '') then concat($familyName, ',', $givenName) else $givenName" />
       			</dc:contributor>
       		</when>
        </choose>
    </template>

    <template match="mods:name[@type='corporate' and @displayLabel!='mapping-hack-default-publisher']">
    	<variable name="code" select="mods:role/mods:roleTerm[@type='code']/text()" />
        
        <choose>
        	<when test="$code = 'dgg'">
        		<dc:contributor>
        			<value-of select="mods:namePart[1]"/>
        		</dc:contributor>
        	</when>
        	<when test="$code = 'edt' and not(../mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code' and .='edt'])">
        		<dc:contributor>
        			<value-of select="mods:namePart[1]"/>
        		</dc:contributor>
        	</when>
        	<when test="$code = 'pbl'">
        		<dc:publisher>
        			<value-of select="mods:namePart[1]" />
        		</dc:publisher>
        	</when>
        </choose>
    </template>
    
     <template match="mods:name[@type='corporate' and @displayLabel='mapping-hack-default-publisher']">
     	<variable name="pblId" select="@ID" />
     	
     	<if test="not(../mods:name[@type='corporate' and mods:role/mods:roleTerm[@type='code']='pbl'])">
       		<dc:publisher>
       			<value-of select="../mods:extension/slub:info/slub:corporation[@ref=$pblId]/slub:university" />
       		</dc:publisher>
       	</if>
    </template>

	<template match="mods:originInfo[@eventType='distribution']/mods:dateIssued[@keyDate='yes']">
		<dc:date>
			<value-of select="substring(. ,1 ,10)" />
		</dc:date>
	</template>
	
	<template match="mods:originInfo[@eventType='publication']">
		<dc:date>
			<value-of select="substring(. ,1 ,10)" />
		</dc:date>
	</template>
	
	<template match="mods:originInfo[@eventType='submission']">
		<dc:date>
			<value-of select="substring(. ,1 ,10)" />
		</dc:date>
	</template>
	
	<template match="mods:originInfo[@eventType='defence']">
		<dc:date>
			<value-of select="substring(. ,1 ,10)" />
		</dc:date>
	</template>
	
	<template match="slub:funding/slub:project">
		<dc:relation>
			<value-of select="." />
		</dc:relation>
	</template>

    <!-- eat all unmatched text content -->
    <template match="text()"/>

</stylesheet>
