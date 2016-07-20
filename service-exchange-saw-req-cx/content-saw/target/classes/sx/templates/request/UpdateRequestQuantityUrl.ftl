<#assign entity=message.args.entity />
<#assign linkedEntity=message.args.linkedEntity />
<#assign properties=entity.properties />
<#if properties.Quantity??>			
<#escape x as x?url>
<#noescape>${instanceConfig.endpoint}</#noescape>/rest/${instanceConfig.organization}/ems/bulk
</#escape>
</#if>