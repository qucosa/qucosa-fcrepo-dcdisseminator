<?xml version="1.0" encoding="UTF-8"?>
<stylesheet version="1.0" 
	xmlns="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:mets="http://www.loc.gov/METS/"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:ddb="http://www.d-nb.de/standards/ddb/"
    xmlns:pc="http://www.d-nb.de/standards/pc/"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:subject="http://www.d.nb.de/standards/subject/"
    xmlns:cc="http://www.d-nb.de/standards/cc/"
    xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/"
    xmlns:myfunc="urn:de:qucosa:xmetadissplus"
    xmlns:dini="http://www.d-nb.de/standards/xmetadissplus/type/"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:urn="http://www.d-nb.de/standards/urn/"
    xmlns:slub="http://slub-dresden.de/"
    xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd
    					http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd
                        http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods.xsd
                        http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dc.xsd
                        http://www.d-nb.de/standards/pc/ http://files.dnb.de/standards/xmetadiss/pc.xsd
                        http://www.d-nb.de/standards/ddb/ http://files.dnb.de/standards/xmetadiss/ddb.xsd
                        http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dcterms.xsd
                        http://www.d.nb.de/standards/subject/ http://files.dnb.de/standards/xmetadiss/subject.xsd
                        http://www.d-nb.de/standards/cc/ http://files.dnb.de/standards/xmetadiss/cc.xsd
                        http://www.ndltd.org/standards/metadata/etdms/1.0/ http://files.dnb.de/standards/xmetadiss/thesis.xsd
                        http://www.w3.org/2001/XMLSchema https://www.w3.org/2009/XMLSchema/XMLSchema.xsd
                        http://www.d-nb.de/standards/xmetadissplus/type/ http://files.dnb.de/standards/xmetadissplus/xmetadissplustype.xsd
                        http://www.d-nb.de/standards/urn/ http://files.dnb.de/standards/xmetadiss/urn.xsd">
	
	<output standalone="yes" encoding="utf-8" media-type="application/xml" indent="yes" method="xml"/>
	<strip-space elements="*"/>
	
	<template match="/mets:mets">
        <oai_dc:dc>
            <apply-templates select="mets:dmdSec[@ID='DMD_000']/mets:mdWrap[@MDTYPE='MODS']/mets:xmlData/mods:mods"/>
        </oai_dc:dc>
    </template>
    
    <template match="mods:mods">
    	<apply-templates select="mods:titleInfo[@usage='primary']"/>
    </template>
    
    <template match="mods:titleInfo/mods:title">
        <dc:title xml:lang="{../@lang}">
            <value-of select="."/>
        </dc:title>
    </template>
    
    <!-- eat all unmatched text content -->
    <template match="text()"/>
	
</stylesheet>