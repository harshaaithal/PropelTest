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
                "DisplayLabel": "${properties.Title}",
				
				
				<#if message.entityChange.entity.properties.RequestedByPersonId?? && message.entityChange.entity.properties.RequestedByPersonId !="NULL">
				"RequestedByPerson": "${message.entityChange.entity.properties.RequestedByPersonId}",
				<#else>
				"RequestedByPerson": "${sawMapping.Default.RequestedByPersonId}",
				</#if>				
				
				<#if message.entityChange.entity.properties.RequestedForPersonId?? && message.entityChange.entity.properties.RequestedForPersonId!="NULL">
				"RequestedForPerson": "${message.entityChange.entity.properties.RequestedForPersonId}",
				<#else>
				"RequestedForPerson": "${sawMapping.Default.RequestedForPersonId}",

				</#if>
				
				
			
				<#if message.entityChange.entity.properties.ServiceDeskGroupId?? && message.entityChange.entity.properties.ServiceDeskGroupId!="NULL">
				"ServiceDeskGroup" :  "${message.entityChange.entity.properties.ServiceDeskGroupId}",
				<#else>
				"ServiceDeskGroup.Name" :  "${sawMapping.Default.AssignmentGroup}",
				</#if>
								
				
				<#--- <#if message.entityChange.entity.properties.OwnedByPersonId?? && message.entityChange.entity.properties.OwnedByPersonId!="NULL">
				"OwnedByPerson" : "${message.entityChange.entity.properties.OwnedByPersonId}",
				<#else>
				"OwnedByPerson" : "${sawMapping.Default.AssigneeId}",
				</#if> -->				
				
				<#if message.entityChange.entity.properties.CmdbCiId?? && message.entityChange.entity.properties.CmdbCiId!="NULL">
				"RegisteredForActualService" : "${message.entityChange.entity.properties.CmdbCiId}",
				<#else>
				"RegisteredForActualService" : "${sawMapping.Default.Service}",
				</#if>
				
				<#--- Till here This has to be modified -->
				
				
				
				
				<#if properties.NewHireLegalEmployeeType??>
				"NewHireDetailsEmployeeType_c":"${properties.NewHireLegalEmployeeType}",
				</#if>
				
				<#if properties.NewHireLegalEndDate??>
				"NewHireDetailsEndDate_c":${properties.NewHireLegalEndDate},
				</#if>
				
				<#if properties.NewHirePreferredName??>
				"NewHirePreferredName_c":"${properties.NewHirePreferredName}",
				</#if>
				
				
				
				<#if properties.ApprovalStatus??>
				"SnowApprovalStatus_c":"${properties.ApprovalStatus}",
				</#if>
				
				<#--
				<#if properties.Quantity??>
				"Quantity":${properties.Quantity},
				</#if>
				-->
				
				<#if properties.NewHireLegalFirstName??>
				"NewHireDetailsLegalFirstName_c":"${properties.NewHireLegalFirstName}",
				</#if>
				
				<#if properties.NewHireLegalLastName??>
				"NewHireDetailsLegalLastName_c":"${properties.NewHireLegalLastName}",
				</#if>
				
				<#if properties.NewHireLegalStartDate??>
				"NewHireDetailsStartDate_c":${properties.NewHireLegalStartDate},
				</#if>
				
				<#if properties.BackOrdered??>
				"SNOWBackOrdered_c":${properties.BackOrdered},
				</#if>
				
				<#if properties.CatalogName??>
				"SNOWCatalogItem_c":"${properties.CatalogName}",
				</#if>
				
				<#if properties.DueDate??>
				"SNOWDueDate_c":${properties.DueDate},
				</#if>
				
				<#if properties.Number??>
				"SNOWRequestItemNumber_c":"${properties.Number}",
				</#if>
				
				<#if properties.RequestTaskNumber??>
				"SNOWRequestItemTaskNumber_c" : "${properties.RequestTaskNumber}",
				</#if>
				"ExternalProcessReference":"${properties.RequestNumber}",
		
				
				
                "Description": <#noescape>${writeJson(properties.Description?replace("\n","<BR>"))}</#noescape>,
                <#if message.args.event?? && (message.args.event == "requestResolved" || message.args.event == "requestCancelled" || message.args.event == "requestClosed")>
                    "Solution": "<@compress_single_line>${sanitize(properties.Solution!localize(context.targetInstance,instances,"DID_NOT_FILL_SOLUTION"))}</@compress_single_line>",
                    <#if properties.CompletionCode?? && message.args.event == "requestResolved" >
                    "CompletionCode": "${sawMapping.Request.CompletionCodeFromCanonical[properties.CompletionCode]}",
                    </#if>
                    <#if message.args.event == "requestCancelled" >
                    "CompletionCode": "WithdrawnbyUser",
                    </#if>
                </#if>
				
				"RequestType": "ServiceRequest",
				"ImpactScope": "${sawMapping.Request.Impact[properties.Impact]}",
                "Urgency": "${sawMapping.Request.Urgency[properties.Urgency]}",
                "RequestAttachments": "{\"complexTypeProperties\":[]}"
        },
            "Comments": [
    
    
     <#if entity.properties.Comments??>
        <#list entity.properties.Comments as comment>
        {
        <#setting time_zone='GMT'>
        <#if comment.createdAt?is_number>
           "CreatedTime": "${comment.createdAt?number_to_datetime?string.long}",
        <#else>
            "CreatedTime": "${comment.createdAt}",
        </#if>
            
         "operator": "${comment.operator}",
        "IsSystem": true,
        "Body": "${comment.description}",
        "PrivacyType": <#if comment.isCustVisible>"PUBLIC"<#else>"PRIVATE"</#if>
        }
            <#if comment_has_next>,</#if>
        </#list>
    </#if>
    
            ],
            "ext_properties": {
                "ExternalSystem": "${findAliasForExtSystem(context.appContext, linkedEntity.instanceType, linkedEntity.instance, entity.instanceType, entity.instance)}",
                "Operation": "Create",
                "ExternalId": "${entity.entityId}",
                "ExternalEntityType": "${entity.entityType}",
                "ExternalStatus": "${properties.OriginalStatus!properties.Status}"
            }
        }
    ]
}

</#escape>