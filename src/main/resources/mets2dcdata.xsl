<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright 2018 Saxon State and University Library Dresden (SLUB)
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<stylesheet xmlns:dc="http://purl.org/dc/elements/1.1/"
            xmlns:mets="http://www.loc.gov/METS/"
            xmlns:mods="http://www.loc.gov/mods/v3"
            xmlns:slub="http://slub-dresden.de/"
            xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:myfunc="urn:de:qucosa:dc"
            xmlns:xs="http://www.w3.org/2001/XMLSchema"
            version="2.0"
            xmlns="http://www.w3.org/1999/XSL/Transform"
            xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd
    					http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd
                        http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods.xsd
                        http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2008/02/11/dc.xsd">

    <output standalone="yes" encoding="utf-8" media-type="application/xml" indent="yes" method="xml"/>
    <strip-space elements="*"/>

    <variable name="documentType" select="/mets:mets/mets:structMap[@TYPE='LOGICAL']/mets:div/@TYPE"/>
    <variable name="documentStatus" select="//mods:originInfo[@eventType='production']/mods:edition[1]"/>

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
        <apply-templates select="mods:originInfo"/>
        <apply-templates select="mods:relatedItem[@type='original']/mods:note[@type='z']"/>
        <apply-templates select="mods:relatedItem[@type='original']"/>
        <apply-templates select="mods:relatedItem[@type='series']"/>
        <apply-templates select="mods:relatedItem[@type='host']"/>
    </template>

    <template match="slub:info">
        <apply-templates select="slub:documentType"/>
        <apply-templates select="." mode="dc:relation"/>
    </template>

    <template match="mods:titleInfo">
        <dc:title>
            <value-of select="mods:title"/>
            <variable name="titleInfoLang" select="@lang"/>
            <if test="../mods:titleInfo[@lang=$titleInfoLang]/mods:subTitle">
                <value-of
                        select="concat(':', string-join(../mods:titleInfo[@lang=$titleInfoLang][not(@type='alternative')]/mods:subTitle, ':'))"/>
            </if>
        </dc:title>
    </template>

    <!-- Document status/version (Begutachtungsstatus) mapping for OAI:DC -->
    <template match="mods:originInfo[@eventType='production']/mods:edition[1]">
        <dc:type>
            <choose>
                <when test="$documentStatus = 'draft'">info:eu-repo/semantics/draft</when>
                <when test="$documentStatus = 'submitted'">info:eu-repo/semantics/submittedVersion</when>
                <when test="$documentStatus = 'published'">info:eu-repo/semantics/publishedVersion</when>
                <when test="$documentStatus = 'accepted'">info:eu-repo/semantics/acceptedVersion</when>
                <when test="$documentStatus = 'updated'">info:eu-repo/semantics/updatedVersion</when>
                <otherwise>info:eu-repo/semantics/publishedVersion</otherwise>
            </choose>
        </dc:type>
    </template>

    <!-- Document type mapping for OAI:DC -->
    <template match="mets:structMap[@TYPE='LOGICAL']/mets:div[1]">
        <dc:type>
            <choose>
                <when test="$documentType = 'contained_work'">bookPart</when>
                <when test="($documentType = 'magister_thesis') or ($documentType = 'diploma_thesis') or ($documentType = 'master_thesis')">
                    masterThesis
                </when>
                <when test="$documentType = 'research_paper'">workingPaper</when>
                <when test="($documentType = 'proceeding') or ($documentType = 'in_proceeding')">conferenceObject</when>
                <when test="$documentType = 'monograph'">book</when>
                <when test="$documentType = 'text'">Other</when>
                <when test="$documentType = 'musical_notation'">MusicalNotation</when>
                <when test="$documentType = 'paper'">StudyThesis</when>
                <when test="$documentType = 'issue'">PeriodicalPart</when>
                <when test="($documentType = 'series') or ($documentType = 'periodical')">Periodical</when>
                <when test="$documentType = 'multivolume_work'">book</when>
                <when test="($documentType = 'habilitation_thesis') or ($documentType = 'doctoral_thesis')">
                    doctoralThesis
                </when>
                <when test="$documentType = 'bachelor_thesis'">bachelorThesis</when>
                <otherwise>
                    <value-of select="@TYPE"/>
                </otherwise>
            </choose>
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
        <dc:subject>
            <value-of select="concat('info:eu-repo/classification/ddc/', .)"/>
        </dc:subject>
    </template>

    <template match="mods:classification[@authority='z']">
        <dc:subject>
            <value-of select="."/>
        </dc:subject>
    </template>

    <template match="mods:classification[@authority='sswd']">
        <dc:subject>
            <value-of select="replace(., ',', ';')"/>
        </dc:subject>
    </template>

    <template match="mods:name[@type='personal']">
        <variable name="familyName" select="mods:namePart[@type='family']"/>
        <variable name="givenName" select="mods:namePart[@type='given']"/>
        <variable name="code" select="mods:role/mods:roleTerm[@type='code']/text()"/>

        <choose>
            <when test="$code = 'aut' or $code = 'cmp' or $code = 'art'">
                <dc:creator>
                    <value-of select="if($familyName != '') then concat($familyName, ',', $givenName) else $givenName"/>
                </dc:creator>
            </when>
            <when test="$code = 'rev' or $code = 'ctb' or $code = 'ths' or $code = 'sad' or $code = 'pbl'
       					or $code = 'ill' or $code = 'edt' or $code = 'oth' or $code = 'trl'">
                <dc:contributor>
                    <value-of select="if($familyName != '') then concat($familyName, ',', $givenName) else $givenName"/>
                </dc:contributor>
            </when>
        </choose>
    </template>

    <template match="mods:name[@type='corporate' and not(@displayLabel='mapping-hack-default-publisher')]">
        <variable name="code" select="mods:role/mods:roleTerm[@type='code']/text()"/>
        <variable name="corporateID" select="@ID"/>

        <choose>
            <when test="$code = 'dgg'">
                <dc:contributor>
                    <value-of select="mods:namePart[1]"/>
                </dc:contributor>
            </when>
            <when test="$code = 'edt' and not(../mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code' and .='edt'])">
                <if test="../mods:extension/slub:info/slub:corporation[@ref=$corporateID]">
                    <dc:creator>
                        <value-of select="concat(mods:namePart[1], '.')"/>
                        <value-of select="../mods:extension/slub:info/slub:corporation[@ref=$corporateID]/slub:*"
                                  separator="."/>
                    </dc:creator>
                </if>
                <if test="not(../mods:extension/slub:info/slub:corporation[@ref=$corporateID])">
                    <dc:contributor>
                        <value-of select="concat(mods:namePart[1], '.')"/>
                        <value-of select="../mods:extension/slub:info/slub:corporation[@ref=$corporateID]/slub:*"
                                  separator="."/>
                    </dc:contributor>
                </if>
            </when>
            <when test="$code = 'pbl'">
                <dc:publisher>
                    <value-of select="mods:namePart[1]"/>
                </dc:publisher>
            </when>
        </choose>
    </template>

    <template match="mods:name[@type='corporate' and @displayLabel='mapping-hack-default-publisher']">
        <variable name="pblId" select="@ID"/>

        <if test="not(../mods:name[@type='corporate' and mods:role/mods:roleTerm[@type='code']='pbl'])">
            <dc:publisher>
                <value-of select="../mods:extension/slub:info/slub:corporation[@ref=$pblId]/slub:university"/>
            </dc:publisher>
        </if>
    </template>

    <!-- Distribution - Datum der Veröffentlichung im Repository -->
    <template match="mods:originInfo[@eventType='distribution']/mods:dateIssued[@keyDate='yes']">
        <choose>
            <when test="string-length(normalize-space(.)) = 0">
                <comment>dc:date could not be created, missing value in
                    mods:originInfo[@eventType='distribution']/mods:dateIssued[@keyDate='yes']
                </comment>
            </when>
            <otherwise>
                <dc:date>
                    <value-of select="myfunc:formatDateTime(.)"/>
                </dc:date>
            </otherwise>
        </choose>
    </template>

    <!-- Jahr der Erstveröffentlichung -->
    <template match="mods:originInfo[@eventType='publication']/mods:dateIssued">
        <choose>
            <when test="string-length(normalize-space(.)) = 0">
                <comment>dc:date could not be created, missing value in
                    mods:originInfo[@eventType='publication']/mods:dateIssued
                </comment>
            </when>
            <otherwise>
                <dc:date>
                    <value-of select="myfunc:formatDateTime(.)"/>
                </dc:date>
            </otherwise>
        </choose>
    </template>

    <!--Datum der Einreichung-->
    <template match="mods:originInfo[@eventType='publication']/mods:dateOther[@type='submission']">
        <choose>
            <when test="string-length(normalize-space(.)) = 0">
                <comment>dc:date could not be created, missing value in
                    mods:originInfo[@eventType='publication']/mods:dateOther[@type='submission']
                </comment>
            </when>
            <otherwise>
                <dc:date>
                    <value-of select="myfunc:formatDateTime(.)"/>
                </dc:date>
            </otherwise>
        </choose>
    </template>

    <!--Datum der Verteidigung-->
    <template match="mods:originInfo[@eventType='publication']/mods:dateOther[@type='defense']">
        <choose>
            <when test="string-length(normalize-space(.)) = 0">
                <comment>dc:date could not be created, missing value in
                    mods:originInfo[@eventType='publication']/mods:dateOther[@type='defense']
                </comment>
            </when>
            <otherwise>
                <dc:date>
                    <value-of select="myfunc:formatDateTime(.)"/>
                </dc:date>
            </otherwise>
        </choose>
    </template>

    <template match="slub:info" mode="dc:relation">
        <variable name="Funder" select="./slub:juristiction"/>
        <variable name="FundingProgram" select="./slub:funding"/>
        <variable name="ProjectID" select="./slub:project/@uid"/>
        <if test="$Funder != '' and $FundingProgram != '' and $ProjectID != ''">
            <dc:relation>
                info:eu-repo/grantAgreement/<value-of select="$Funder"/>/<value-of select="$FundingProgram"/>/<value-of
                    select="$ProjectID"/>
            </dc:relation>
        </if>
    </template>

    <template match="slub:info[slub:collections/slub:collection='nonOA']">
        <dc:rights>info:eu-repo/semantics/restrictedAccess</dc:rights>
    </template>

    <template match="slub:info[not(slub:collections/slub:collection = 'nonOA')]">
        <dc:rights>info:eu-repo/semantics/openAccess</dc:rights>
    </template>

    <template match="mods:relatedItem[@type='original']/mods:note[@type='z']">
        <dc:source>
            <value-of select="."/>
        </dc:source>
    </template>

    <template match="mods:relatedItem[@type='original']">
        <if test="not(../mods:relatedItem[@type='original']/mods:note[@type='z'])">
            <variable name="startPage" select="../mods:part[@type='section']/mods:extent[@unit='pages']/mods:start"/>
            <variable name="endPage" select="../mods:part[@type='section']/mods:extent[@unit='pages']/mods:end"/>
            <variable name="title" select="mods:titleInfo/mods:title"/>
            <choose>
                <when test="$documentType = 'article'">
                    <dc:source>
                        <value-of select="concat($title, ' ', mods:part[@type='volume']/mods:detail/mods:number)"/>
                        <value-of select="concat(' (', mods:part[@type='issue']/mods:detail/mods:number, ')')"/>
                        <value-of select="concat(', S. ', $startPage, '-', $endPage)"/>
                        <value-of
                                select="if (mods:identifier[@type='issn']) then concat('. ISSN: ', mods:identifier[@type='issn']) else ''"/>
                    </dc:source>
                </when>
                <!-- Document type `in_book` is to be replaced by `contained_work` in the future. -->
                <!-- As soon as conferences are supported, `in_proceeding` and `contained_work` need to be different. -->
                <when test="($documentType = 'in_proceeding') or ($documentType = 'contained_work') or ($documentType = 'in_book')">
                    <dc:source>
                        <value-of select="concat($title, ': ', mods:titleInfo/mods:subTitle, '.')"/>
                        <value-of
                                select="concat(' ', mods:originInfo/mods:place/mods:placeTerm, ': ' , mods:originInfo/mods:publisher)"/>
                        <value-of select="concat(', S. ', $startPage, '-', $endPage)"/>
                        <value-of
                                select="if(mods:identifier[@type='isbn']) then concat('. ISBN: ', mods:identifier[@type='isbn']) else ''"/>
                    </dc:source>
                </when>
            </choose>
        </if>
    </template>

    <template match="mods:relatedItem[@type='series']">
        <if test="not(../mods:relatedItem[@type='original']/mods:note[@type='z']) and $documentType='monograph'">
            <dc:source>
                <value-of select="mods:titleInfo/mods:title"/>
                <value-of
                        select="if(mods:part[@type='volume']) then concat(' ; Bd. ', mods:part[@type='volume']/mods:detail/mods:number) else ''"/>
                <value-of
                        select="if(mods:identifier[@type='issn']) then concat('. ISSN: ', mods:identifier[@type='issn']) else ''"/>
            </dc:source>
        </if>
    </template>

    <template match="mods:relatedItem[@type='host']">
        <if test="not(../mods:relatedItem[@type='original']/mods:note[@type='z']) and $documentType='monograph'">
            <dc:source>
                <value-of select="mods:titleInfo/mods:title"/>
                <value-of
                        select="if(mods:part[@type='volume']) then concat('. Bd. ', mods:part[@type='volume']/mods:detail/mods:number) else ''"/>
                <value-of
                        select="if(mods:identifier[@type='isbn']) then concat('. ISBN: ', mods:identifier[@type='isbn']) else ''"/>
            </dc:source>
        </if>
    </template>

    <!-- eat all unmatched text content -->
    <template match="text()"/>

    <!-- Helper functions and templates -->

    <function name="myfunc:formatDateTime" as="xs:string">
        <param name="value" as="xs:string"/>
        <choose>
            <when test="string-length($value)=4">
                <value-of select="$value"/>
            </when>
            <when test="contains($value, 'T')">
                <value-of
                        select="format-dateTime(xs:dateTime(myfunc:formatTimezoneHour($value)), '[Y0001]-[M01]-[D01]')"/>
            </when>
            <otherwise>
                <value-of select="format-date(xs:date($value), '[Y0001]-[M01]-[D01]')"/>
                <!--<value-of select="." />-->
            </otherwise>
        </choose>
    </function>

    <function name="myfunc:formatTimezoneHour" as="xs:string">
        <param name="value" as="xs:string"/>
        <choose>
            <when test="matches($value, '[+|-]\d{4}$')">
                <variable name="a" select="substring($value, 1, string-length($value)-2)"/>
                <variable name="b" select="substring($value, string-length($value)-1)"/>
                <value-of select="concat($a, ':', $b)"/>
            </when>
            <otherwise>
                <value-of select="$value"/>
            </otherwise>
        </choose>
    </function>

</stylesheet>
