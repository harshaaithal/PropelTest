<#macro compress_single_line><#local captured><#nested></#local>${ captured?replace("^\\s+|\\s+$|\\n|\\r", " ", "rm") }</#macro>

<#escape x as x?json_string>

    <#assign writeJson='com.hp.ccue.serviceExchange.adapter.freemarker.WriteJson'?new()/>
    <#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
    <#assign findAliasForExtSystem='com.hp.ccue.serviceExchange.adapter.freemarker.caseex.FindAliasForExternalSystem'?new() />
    <#assign sanitize='com.hp.ccue.serviceExchange.adapter.freemarker.StringSanitizer'?new()/>
    <#assign localize='com.hp.ccue.serviceExchange.adapter.saw.util.SXSAWImplProperties'?new()/>
    <#assign instances=loadConfig(context.configuration, "saw/instances") />

    <#assign entitiesMapping=loadConfig(context.configuration, "saw/entities") />
    <#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

    <#assign entity=message.args.entity />
    <#assign linkedEntity=message.args.linkedEntity />
    <#assign properties=entity.properties />

{
    "newEntity":{
        "entity":{
            "entity_type": "Request",
            "properties": {
                "Id": "${linkedEntity.entityId}",
                "DisplayLabel": "${properties.Title}",
                "Description": "<@compress_single_line>${sanitize(properties.Description)}</@compress_single_line>",
                
                <#if message.entityChange.entity.properties.RequestedForPersonId?? && message.entityChange.entity.properties.RequestedForPersonId!="NULL">
				"RequestedForPerson": "${message.entityChange.entity.properties.RequestedForPersonId}",
				<#else>
				"RequestedForPerson": "${sawMapping.Default.RequestedForPersonId}",

				</#if>
				
				
                <#if message.args.event?? && (message.args.event == "requestResolved" || message.args.event == "requestCancelled")>
                  "Solution": "<@compress_single_line>${sanitize(properties.Solution!localize(context.targetInstance,instances,"DID_NOT_FILL_SOLUTION"))}</@compress_single_line>",
                  <#if properties.CompletionCode?? && message.args.event == "requestResolved" >
                    "CompletionCode": "${sawMapping.Request.CompletionCodeFromCanonical[properties.CompletionCode]}",
                  </#if>
                  <#if message.args.event == "requestCancelled" >
                    "CompletionCode": "WithdrawnbyUser",
                  </#if>
                </#if>
                "ImpactScope": "${sawMapping.Request.Impact[properties.Impact]}",
                "Urgency": "${sawMapping.Request.Urgency[properties.Urgency]}"
            },
            "Comments": [
			<#if message.MergedComments??>
               <#list message.MergedComments as comment>
                   {
                       <#--"CreatedTime": ${comment.createdAt?string["0"]}, 
			 "CreatedTime": ${comment.createdAt?iso_utc_ms}, 

			 "HPEVar1":  "${comment.createdAt?number_to_datetime?iso_utc_ms}",-->
			<#setting time_zone='GMT'>

		     

		      <#if comment.createdAt?is_number>
		     
      		      "CreatedTime":  "${comment.createdAt?number_to_datetime?string.long}",
      		      <#else>
      		      "CreatedTime":  "${comment.createdAt}",
		      </#if>
		     
                      <#-- "IsSystem": true, -->
                       "Body": "<br>${comment.description}",
                       <#-- "PrivacyType": <#if comment.isCustVisible>"PUBLIC"<#else>"PRIVATE"</#if> -->
					   "Name": "${comment.operator}"
                   }
                   <#if comment_has_next>,</#if>
               </#list>
               </#if> 
			   ],
            "ext_properties": {
                "ExternalSystem": "${findAliasForExtSystem(context.appContext, linkedEntity.instanceType, linkedEntity.instance, entity.instanceType, entity.instance)}",
                "Operation": "Update",
                "ExternalStatus": "${properties.Status}"
            }
        }
    }
}

</#escape>