<#-- @ftlvariable name="instanceConfig" type="java.util.Map" -->
<#-- @ftlvariable name="message" type="java.util.Map" -->
<#include "UA_SAW_CUSTOM_FIELDS.ftl"/>
<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign entitiesMapping=loadConfig(context.configuration, "saw/entities") />
<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

<#escape x as x?url>
<#noescape>${instanceConfig.endpoint}</#noescape>
/rest/${instanceConfig.organization}/ems/${sawMapping.entityType[message.entityChange.entityType]}/${message.entityChange.entityId}?layout=
${entitiesMapping.incident.fieldNames?join(",")},${UA_SAW_CUSTOM_FIELDS}
</#escape>
