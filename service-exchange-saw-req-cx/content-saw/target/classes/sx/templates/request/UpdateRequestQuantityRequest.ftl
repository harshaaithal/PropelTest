<#macro compress_single_line><#local captured><#nested></#local>${ captured?replace("^\\s+|\\s+$|\\n|\\r", " ", "rm") }</#macro>
<#escape x as x?json_string>

<#assign writeJson='com.hp.ccue.serviceExchange.adapter.freemarker.WriteJson'?new()/>
<#assign findKey='com.hp.ccue.serviceExchange.adapter.freemarker.FindKeyForValue'?new()/>
<#assign findAliasForExtSystem='com.hp.ccue.serviceExchange.adapter.freemarker.caseex.FindAliasForExternalSystem'?new() />
<#assign sanitize='com.hp.ccue.serviceExchange.adapter.freemarker.StringSanitizer'?new()/>
<#assign localize='com.hp.ccue.serviceExchange.adapter.saw.util.SXSAWImplProperties'?new()/>
<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign instances=loadConfig(context.configuration, "saw/instances") />


<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

<#assign entity=message.args.entity />
<#assign linkedEntity=message.args.linkedEntity />
<#assign properties=entity.properties />
{
    "entities": [
        {
            "entity_type": "Request",
            "properties": {
             "Id" : "${message.tmp.sawEntityId}",
			 "Quantity" : ${properties.Quantity}
            }
            
        }
    ],
	"operation":"UPDATE"
}

</#escape>