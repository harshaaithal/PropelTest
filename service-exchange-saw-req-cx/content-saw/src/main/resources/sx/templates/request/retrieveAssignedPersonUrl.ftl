<#-- @ftlvariable name="instanceConfig" type="java.util.Map" -->
<#-- @ftlvariable name="message" type="java.util.Map" -->

<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign entitiesMapping=loadConfig(context.configuration, "saw/entities") />
<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

<#escape x as x?url>
<#noescape>${instanceConfig.endpoint}</#noescape>
	<#if message.args.entity.properties.AssignedToEmail??>
	       <#assign Personname=message.args.entity.properties.AssignedToEmail/>
        <#else>
          <#assign Personname=sawMapping.Default.AssignedToEmail/> 
           </#if> 
	
/rest/${instanceConfig.organization}/ems/Person?layout=Name,Id&filter=${"Upn='"+Personname+"'"}&meta=totalCount
</#escape>




