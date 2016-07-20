<#-- @ftlvariable name="instanceConfig" type="java.util.Map" -->
<#-- @ftlvariable name="message" type="java.util.Map" -->

<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign entitiesMapping=loadConfig(context.configuration, "saw/entities") />
<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

<#escape x as x?url>
<#noescape>${instanceConfig.endpoint}</#noescape>
<#if message.args.entity.properties.AssignedGroupName??>
	<#assign Groupname=message.args.entity.properties.AssignedGroupName/>
<#else>
	<#assign Groupname=sawMapping.Default.AssignmentGroup/>
</#if>
/rest/${instanceConfig.organization}/ems/PersonGroup?layout=Name,Id&filter=${"Name='"+Groupname+"'"}+&meta=totalCount
</#escape>
