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
<#setting time_zone='GMT'>
{
    "entities": [
        {
            "entity_type": "Incident",
            "properties": {
                "DisplayLabel": "${properties.Title}",
				<#-- "UALocation_c": "${properties.UALocation}", -->
				<#-- "UASubCategory_c": "${properties.UASubCategory}", -->
				<#if properties.UASolTime?? && properties.UASolTime != "">
					"SolvedTime": ${properties.UASolTime},
				</#if>
				<#if properties.UAContactType??>
					<#if properties.UAContactType == "email">
						"ReqCreationSource_c": ${properties.UAContactType},
					<#else>
						"ReqCreationSource_c": "snow",
					</#if>
				</#if>

				<#if properties.UAClosedtime?? && properties.UAClosedtime != "">
					"CloseTime": ${properties.UAClosedtime},
				</#if>
				<#if properties.UAEscalation?? && properties.UAEscalation == "0">
					"Escalated": false,
				</#if>
				<#if properties.UAEscalation?? && properties.UAEscalation == "1">
					"Escalated": true,
				</#if>
				
				<#if properties.Status??>
					<#if properties.Status=="Ready" || properties.Status=="InProgress">
					"Status" : "${properties.Status}",
					</#if>
				</#if>
				
				<#if message.entityChange.entity.properties.UAAssignedToId?? && message.entityChange.entity.properties.UAAssignedToId != "">
					"AssignedPerson": "${message.entityChange.entity.properties.UAAssignedToId}",
				<#else>
					"AssignedPerson.Email": "${sawMapping.Default.IMAssignedPersonEmail}", 
					
					
				</#if>
				<#if properties.UAClosedByPerson?has_content>
				
				<#if message.entityChange.entity.properties.UAClosedByPersonId?? && message.entityChange.entity.properties.UAClosedByPersonId != "">
					"ClosedByPerson": "${message.entityChange.entity.properties.UAClosedByPersonId}",
				<#else>
					"ClosedByPerson.Email": "${sawMapping.Default.IMClosedByPersonEmail}", 
					
				</#if>
				</#if>
				
				<#if message.entityChange.entity.properties.UAExpertGroupId?? && message.entityChange.entity.properties.UAExpertGroupId != "">
					"ExpertGroup": "${message.entityChange.entity.properties.UAExpertGroupId}",
					"AssignedGroup": "${message.entityChange.entity.properties.UAExpertGroupId}",
				<#else>
					"ExpertGroup.Name": "${sawMapping.Default.IMAssignmentGroupName}", 
					"AssignedGroup.Name": "${sawMapping.Default.IMAssignmentGroupName}", 
				</#if>
				"ExternalProcessReference" :  "${properties.UANumber}",
				"RegisteredForLocation.Name": "${properties.UALocation}",
				<#-- "RequestedByPerson.Upn": "${properties.UARequestedByPerson}", -->
				<#if message.entityChange.entity.properties.UARequestedByPersonId?? && message.entityChange.entity.properties.UARequestedByPersonId != "">
					"RequestedByPerson": "${message.entityChange.entity.properties.UARequestedByPersonId}",
				<#else>
					"RequestedByPerson.Email": "${sawMapping.Default.IMRequestedByPersonEmail}", 
					<#-- "RequestedByPerson.Upn": "harsha.s5@hpe.com", -->
				</#if>
                "Description": <#noescape>${writeJson(properties.Description)}</#noescape>,
                <#if message.args.event?? && (message.args.event == "incidentResolved" || message.args.event == "incidentCancelled" || message.args.event == "incidentClosed")>
                    "Solution": "<@compress_single_line>${sanitize(properties.Solution!localize(context.targetInstance,instances,"DID_NOT_FILL_SOLUTION"))}</@compress_single_line>",
                    <#if properties.CompletionCode?? && message.args.event == "incidentResolved" >
                    "CompletionCode": "${sawMapping.Incident.CompletionCodeFromCanonical[properties.CompletionCode]}",
                    </#if>
                    <#if message.args.event == "incidentCancelled" >
                    "CompletionCode": "WithdrawnbyUser",
                    </#if>
                </#if>
                "ImpactScope": "${sawMapping.Incident.Impact[properties.Impact]}",
                "Urgency": "${sawMapping.Incident.Urgency[properties.Urgency]}",
	
                <#if message.entityChange.entity.properties.UARegisteredForActualServiceId?? && message.entityChange.entity.properties.UARegisteredForActualServiceId != "">
                	"RegisteredForActualService": "${message.entityChange.entity.properties.UARegisteredForActualServiceId}",
                <#else>
                	"RegisteredForActualService": "${sawMapping.Default.Service}",
                </#if>
        
		<#if message.entityChange.entity.properties.UACustCategoryId?? && message.entityChange.entity.properties.UACustCategoryId != "">
			"Category": "${message.entityChange.entity.properties.UACustCategoryId}",
		<#else>
			"Category": "${sawMapping.Default.CategoryId}",
		</#if>         
                "IncidentAttachments": "{\"complexTypeProperties\":[]}"
        },
            "Comments": [
    	       <#if properties.Comments??>
               <#list properties.Comments as comment>
                   {
                       <#-- "CreatedTime": ${comment.createdAt?string["0"]}, -->
                        <#if comment.createdAt?is_number>
            			"CreatedTime": "${comment.createdAt?number_to_datetime?string.long}",
            		<#else>
            			"CreatedTime": "${comment.createdAt}",
            		</#if>
                      <#-- "IsSystem": true, -->
                       "Body": "${comment.description}",
                       <#-- "PrivacyType": <#if comment.isCustVisible>"PUBLIC"<#else>"PRIVATE"</#if> -->
					   "Name": "${comment.operator}"
                   }
                   <#if comment_has_next>,</#if>
               </#list>
               </#if> 
            ],
            "ext_properties": {
                "ExternalSystem": "${findAliasForExtSystem(context.appContext, linkedEntity.instanceType, linkedEntity.instance, entity.instanceType, entity.instance)}",
                "Operation": "Create",
                "ExternalId": "${properties.UANumber}",
                "ExternalEntityType": "${entity.entityType}",
                "ExternalStatus": "${properties.OriginalStatus!properties.Status}"
            }
        }
    ]
}

</#escape>