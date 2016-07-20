<#-- @ftlvariable name="message" type="java.util.Map" -->
<#-- @ftlvariable name="context" type="java.util.Map" -->
<#-- @ftlvariable name="loadConfig" type="com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig" -->
<#-- @ftlvariable name="parseJson" type="com.hp.ccue.serviceExchange.adapter.freemarker.ParseJson" -->
<#-- @ftlvariable name="sanitize" type="com.hp.ccue.serviceExchange.adapter.freemarker.StringSanitizer" -->
<#-- @ftlvariable name="findKey" type="com.hp.ccue.serviceExchange.adapter.freemarker.FindKeyForValue" -->
<#-- @ftlvariable name="findExtSystemForAlias" type="com.hp.ccue.serviceExchange.adapter.freemarker.caseex.FindExternalSystemForAlias" -->

<#assign writeJson='com.hp.ccue.serviceExchange.adapter.freemarker.WriteJson'?new()/>
<#assign parseJson='com.hp.ccue.serviceExchange.adapter.freemarker.ParseJson'?new()/>
<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign findKey='com.hp.ccue.serviceExchange.adapter.freemarker.FindKeyForValue'?new()/>
<#assign findExtSystemForAlias='com.hp.ccue.serviceExchange.adapter.freemarker.caseex.FindExternalSystemForAlias'?new() />
<#assign sanitize='com.hp.ccue.serviceExchange.adapter.freemarker.StringSanitizer'?new()/>



<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />

<#assign entityChange=message.entityChange />
<#assign data=entityChange.data />
<#assign entityProperties=data.response.properties />
<#assign extProperties=data.response.ext_properties />
<#assign relatedProperties=data.response.related_properties />
<#assign attachments=parseJson(data.response.properties.RequestAttachments) />
<#-- Handle the description so that the final result is *not* empty -->
<#assign sanitizedDescription=sanitize(entityProperties.Description) />
<#if sanitizedDescription == "">
    <#assign description=" " />
<#else>
    <#assign description=sanitizedDescription />
</#if>


<#macro compress_single_line><#local captured><#nested></#local>${ captured?replace("^\\s+|\\s+$|\\n|\\r", " ", "rm") }</#macro>

<#escape x as x?json_string>
{
  "event": "${entityChange.changeReason}",
  "entity": {
    "instanceType": "${entityChange.instanceType}",
    "instance": "${entityChange.instance}",
    "entityType": "Request",
    "entityId": "${entityChange.entityId}",

    "properties": {       
        "Title":"${entityProperties.DisplayLabel}"
        ,"Description": "<@compress_single_line>${description}</@compress_single_line>"
        ,"Urgency": "${findKey(sawMapping.Request.Urgency, entityProperties.Urgency)}"        
        <#if entityProperties.Status??>
        ,"Status": "${findKey(sawMapping.Request.Status, entityProperties.Status)}"
        ,"OriginalStatus": "${entityProperties.Status}"
        </#if>
        <#if entityProperties.ExpectedCompletionTime??>
        ,"ExpectedCompletionTime":${entityProperties.ExpectedCompletionTime}
		</#if>
	 <#if entityProperties.SNOWRequestItemNumber_c??>	
        ,"RequestItem" : "${entityProperties.SNOWRequestItemNumber_c}"
        </#if>
        ,"PhaseId" : "${entityProperties.PhaseId}"        
        ,"Impact": "${findKey(sawMapping.Request.Impact, entityProperties.ImpactScope)}"
        <#if entityProperties.SNOWRequestItemTaskNumber_c??>
        ,"RequestTicketType" : "RequestTask"        
		<#else>
		,"RequestTicketType" : "RequestItem"
		</#if>       
        <#if entityProperties.Solution??>
		,"Solution": "<@compress_single_line>${sanitize(entityProperties.Solution!"???")}</@compress_single_line>"</#if>
        <#if entityProperties.CompletionCode??>
        ,"CompletionCode": "${sawMapping.Request.CompletionCodeToCanonical[entityProperties.CompletionCode]}"
        </#if>
        <#if attachments.complexTypeProperties??>  
        ,"Attachments": [
            <#list attachments.complexTypeProperties as attachment>
                <#assign attachmentProperties=attachment.properties/>
                {
                    "id": "${attachmentProperties.id}",
                    "name": "${attachmentProperties.file_name}",
                    "type": "${attachmentProperties.mime_type}",
                    "size": ${attachmentProperties.size?string("0")}
                }<#if attachment_has_next>,</#if>
            </#list>
        ]
        </#if>
        ,"Comments": [
            <#if entityChange.entity.Comments?has_content> <#-- <#if entityChange.entity.Comments??> -->
                <#assign firstComment = true />
                    <#list entityChange.entity.Comments.Comment as comment> <#-- <#list entityChange.entity.Comments as comment> -->
                    <#if !comment.IsSystem>
                        

                        <#assign commentBody = comment.CommentBody/>
                        <#assign isCommentExternal = commentBody?matches("\\(\\s([0-9]*).*?\\):.*") />
                        <#if isCommentExternal>
                            <#assign commentBody = commentBody?substring(commentBody?index_of("):") + 2)?trim />
                            <#assign externalCommentTime = isCommentExternal?groups[1] />
                        </#if>
                    <#if comment.CommentFrom != "ExternalServiceDesk">
                    <#if !firstComment>,</#if>
                    {
                    "id": "${comment.CommentId}",
                    "type": "",
                    "completeDescription": "${comment.CommentBody}",
                    "description": "<@compress_single_line>${sanitize(commentBody)}</@compress_single_line>",
		    
                    "isCustVisible": <#if comment.PrivacyType == "PUBLIC">true<#else>false</#if>,
                    "createdAt": <#if isCommentExternal>${externalCommentTime}<#else>${comment.CreateTime?string["0"]}</#if>,
                    "operator": "${comment.Submitter}"
                 
                    }
                        <#assign firstComment = false />
                    </#if>
                    </#if>
                    </#list>
            </#if>
        ]
  }
  },
  "linkedEntity": {
    <#-- "initiator": we-dont-know, -->
    "instanceAlias": "${data.externalInstanceAlias}",
    <#assign alias=findExtSystemForAlias(context.appContext, entityChange.instanceType, entityChange.instance, data.externalInstanceAlias)!"" />
    <#if alias?has_content>
      "instanceType": "${alias.targetInstanceType}",
      "instance": "${alias.targetInstance}",
    </#if>
    <#if extProperties.ExternalEntityType??>"entityType": "${extProperties.ExternalEntityType}",</#if>
    <#if extProperties.ExternalId??>"entityId": "${extProperties.ExternalId}",</#if>
    "properties": {
        "Attachments": []
      <#if entityChange.ExternalReference?? && entityChange.ExternalReference.properties.ExternalStatus??>,"Status": "${entityChange.ExternalReference.properties.ExternalStatus}"</#if>
    }
  },

  <#-- TODO find better way to clean input message -->
  "entityChange": ""
}
</#escape>