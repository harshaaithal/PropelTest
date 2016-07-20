<#-- @ftlvariable name="instanceConfig" type="java.util.Map" -->
<#-- @ftlvariable name="message" type="java.util.Map" -->

<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign entitiesMapping=loadConfig(context.configuration, "saw/entities") />
<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

<#escape x as x?url>
<#noescape>${instanceConfig.endpoint}</#noescape>

<#if message.args.entity.properties.CmdbCiName??>
	<#assign CIName=message.args.entity.properties.CmdbCiName/>

<#else>
	<#assign CIName=sawMapping.Default.CIName/>

</#if>
	/rest/${instanceConfig.organization}/ems/ActualService?layout=DisplayLabel,Id&filter=${"DisplayLabel='"+CIName+"'"}&meta=totalCount

</#escape>

