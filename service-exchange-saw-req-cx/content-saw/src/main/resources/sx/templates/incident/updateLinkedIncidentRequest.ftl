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
            "entity_type": "Incident",
            "properties": {
                "Id": "${linkedEntity.entityId}",
                "DisplayLabel": "${properties.Title}",
                "Description": "<@compress_single_line>${sanitize(properties.Description)}</@compress_single_line>",
				<#if properties.Status??>
					<#if properties.Status=="Ready" || properties.Status=="InProgress" || properties.Status=="Pending">
					"Status" : "${properties.Status}",
					</#if>
				</#if>
				
                <#if message.args.event?? && (message.args.event == "incidentResolved" || message.args.event == "incidentCancelled")>
                  "Solution": "<@compress_single_line>${sanitize(properties.Solution!localize(context.targetInstance,instances,"DID_NOT_FILL_SOLUTION"))}</@compress_single_line>",
                  <#if properties.CompletionCode?? && message.args.event == "incidentResolved" >
                    "CompletionCode": "${sawMapping.Incident.CompletionCodeFromCanonical[properties.CompletionCode]}",
                  </#if>
                  <#if message.args.event == "incidentCancelled" >
                    "CompletionCode": "WithdrawnbyUser",
                  </#if>
                  <#if properties.UASolTime?? && properties.UASolTime != "">
			"SolvedTime": ${properties.UASolTime},
		  </#if>
		  <#if properties.UAClosedtime?? && properties.UAClosedtime != "">
			"CloseTime": ${properties.UAClosedtime},
		  </#if>
                </#if>
				<#if message.entityChange.entity.properties.UAAssignedToId?has_content>
					<#if message.entityChange.entity.properties.UAAssignedToId?? && message.entityChange.entity.properties.UAAssignedToId != "">
						"ExpertAssignee": "${message.entityChange.entity.properties.UAAssignedToId}",
					<#else>
						<#-- "ExpertAssignee.Upn": "harsha.s5@hpe.com", -->
						"ExpertAssignee.Email": "${sawMapping.Default.IMAssignedPersonEmail}",
					</#if>
				</#if>
				<#if properties.UAClosedByPerson?has_content>
				<#-- "ClosedByPerson.Upn": "${properties.UAClosedByPerson}", -->
				<#if message.entityChange.entity.properties.UAClosedByPersonId?? && message.entityChange.entity.properties.UAClosedByPersonId != "">
					"ClosedByPerson": "${message.entityChange.entity.properties.UAClosedByPersonId}",
				<#else>
					<#-- "ClosedByPerson.Upn": "harsha.s5@hpe.com", -->
					"ClosedByPerson.Email": "${sawMapping.Default.IMAssignedPersonEmail}",
				</#if>
				</#if>
				
				<#if message.entityChange.entity.properties.UAExpertGroupId?? && message.entityChange.entity.properties.UAExpertGroupId != "">
					"ExpertGroup": "${message.entityChange.entity.properties.UAExpertGroupId}",
					<#-- "ServiceDeskGroup": "${message.entityChange.entity.properties.UAExpertGroupId}", -->
				<#else>
					"ExpertGroup.Name": "${properties.UAExpertGroup}",
					<#-- "ServiceDeskGroup.Name": "${properties.UAExpertGroup}", -->
				</#if>
				<#if message.entityChange.entity.properties.UARegisteredForActualServiceId?? && message.entityChange.entity.properties.UARegisteredForActualServiceId != "">
		                	"RegisteredForActualService": "${message.entityChange.entity.properties.UARegisteredForActualServiceId}",
		                <#else>
		                	"RegisteredForActualService": "${sawMapping.Default.Service}",
		                </#if>
        
				<#if message.entityChange.entity.properties.UACustCategoryId?? && message.entityChange.entity.properties.UACustCategoryId != "">
					"Category": "${message.entityChange.entity.properties.UACustCategoryId}",
				<#else>
					"Category": "11599",
				</#if>
				"RegisteredForLocation.Name": "${properties.UALocation}",
				<#-- "RequestedByPerson.Upn": "${properties.UARequestedByPerson}", -->
				<#if message.entityChange.entity.properties.UARequestedByPersonId?? && message.entityChange.entity.properties.UARequestedByPersonId != "">
					"RequestedByPerson": "${message.entityChange.entity.properties.UARequestedByPersonId}"
				<#else>
					<#-- "RequestedByPerson.Upn": "harsha.s5@hpe.com", -->
					"RequestedByPerson.Email" : "${sawMapping.Default.IMAssignedPersonEmail}"
				</#if>
				
		<#if properties.Impact??>
			 <#assign currentImpact=sawMapping.Incident.Impact[properties.Impact] />
		<#--	<#if message.oldEntity.entity.properties.ImpactScope!=currentImpact && currentImpact == "MultipleUsers" && message.oldEntity.entity.properties.ImpactScope != "SingleUser">
		-->
			<#if properties.Impact!="I3" >
					,"ImpactScope": "${sawMapping.Incident.Impact[properties.Impact]}"
			<#elseif  message.oldEntity.entity.properties.ImpactScope!=currentImpact && message.oldEntity.entity.properties.ImpactScope != "SingleUser">
					,"ImpactScope": "${sawMapping.Incident.Impact[properties.Impact]}"
			</#if>	


		</#if>
		<#if properties.Urgency??>
			 <#assign currentUrgency=sawMapping.Incident.Urgency[properties.Urgency] />
			<#--
			<#if message.oldEntity.entity.properties.Urgency!=currentImpact && currentUrgency == "SlightDisruption" && message.oldEntity.entity.properties.Urgency!= "NoDisruption">
			-->
			<#if properties.Urgency!="U3" >
			,"Urgency": "${sawMapping.Incident.Urgency[properties.Urgency]}"
			<#elseif message.oldEntity.entity.properties.Urgency!=currentImpact  && message.oldEntity.entity.properties.Urgency!= "NoDisruption">
			,"Urgency": "${sawMapping.Incident.Urgency[properties.Urgency]}"
			<#--
			<#else>
					"Urgency": "${message.oldEntity.entity.properties.Urgency}"
			-->
			</#if>	
		</#if>
                
            },
            "Comments": [
			<#if message.MergedComments??>
               <#list message.MergedComments as comment>
                   {
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