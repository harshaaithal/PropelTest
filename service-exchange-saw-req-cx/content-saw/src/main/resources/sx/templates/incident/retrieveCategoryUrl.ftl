<#-- @ftlvariable name="instanceConfig" type="java.util.Map" -->
<#-- @ftlvariable name="message" type="java.util.Map" -->

<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign entitiesMapping=loadConfig(context.configuration, "saw/entities") />
<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

<#escape x as x?url>
<#noescape>${instanceConfig.endpoint}</#noescape>
<#if message.args.entity.properties.UASubCategory?? && message.args.entity.properties.UASubCategory != "">
/rest/${instanceConfig.organization}/ems/ITProcessRecordCategory?layout=DisplayLabel,Id&filter=${"(DisplayLabel='"+message.args.entity.properties.UASubCategory+"' and  Level1Parent='"+message.args.entity.properties.UACategory+"')"}&meta=totalCount
<#else>
/rest/${instanceConfig.organization}/ems/ITProcessRecordCategory?layout=DisplayLabel,Id&filter=${"(DisplayLabel='"+message.args.entity.properties.UACategory+"')"}&meta=totalCount
</#if>
</#escape>
