<#-- @ftlvariable name="doc" type="java.util.Map" -->
<#-- @ftlvariable name="writeJson" type="com.hp.ccue.serviceExchange.adapter.freemarker.WriteJson" -->
<#-- @ftlvariable name="parseJson" type="com.hp.ccue.serviceExchange.adapter.freemarker.ParseJson" -->

<#assign writeJson='com.hp.ccue.serviceExchange.adapter.freemarker.WriteJson'?new()/>
<#assign parseJson='com.hp.ccue.serviceExchange.adapter.freemarker.ParseJson'?new()/>
<#assign result = doc.result/>
<#assign properties = result.entities[0].properties/>
<#assign HPEcomments = parseJson(properties.Comments!"{}") />


<#assign sanitize='com.hp.ccue.serviceExchange.adapter.freemarker.StringSanitizer'?new()/>
<#assign findAliasForExtSystem='com.hp.ccue.serviceExchange.adapter.freemarker.caseex.FindAliasForExternalSystem'?new() />

<#function compress_single_line captured><#return captured?replace("^\\s+|\\s+$|\\n|\\r", " ", "rm") ></#function>

<#assign entity=message.args.entity />
<#assign linkedEntity=message.args.linkedEntity />
<#-- <#assign properties=entity.properties /> -->

<#escape x as x?json_string>
<#setting time_zone='GMT'>
{

"MergedComments":[
	
	
    <#assign firstComment = true />
    <#assign comments = message.args.entity.properties.Comments! /> 
    
	
	 <#if comments??>
        <#list comments as comment>

            <#assign isAlreadyInSaw = false />
            <#-- <#if doc?? && doc.result?? && doc.result.Comments??> 
			
                <#list doc.result.Comments as cc> -->
				<#if HPEcomments.Comment?has_content>
    
				<#list HPEcomments.Comment as cc>
	
                    <#assign source = findAliasForExtSystem(context.appContext, linkedEntity.instanceType, linkedEntity.instance, entity.instanceType, entity.instance) /> 
                    <#assign commentDesc = cc.CommentBody!"" />
                    <#assign sawCommentTime = cc.CreateTime?string["0"] />
                    <#assign api = cc.ActualInterface />
					<#assign commentFrom = cc.CommentFrom />
					<#if api == "API">
						<#if commentFrom == "ExternalServiceDesk">
							<#assign commentTime = cc.CommentBody?substring(1,cc.CommentBody?index_of("-"))?replace("-","")?replace(" ","")?replace("(","") />
							
							<#setting time_zone='GMT'>
							<#-- <#if comment.createdAt?string["0"] == commentTime> -->
							<#if comment.createdAt?is_number>
							<#assign CommentcreatedAt=comment.createdAt?number_to_datetime?string.long/>
							<#else>
							<#assign CommentcreatedAt=comment.createdAt/>
							</#if>
							
							<#if CommentcreatedAt?replace(" ","") == commentTime>
									<#assign isAlreadyInSaw = true />
									<#break />
							</#if>
						</#if>
					</#if>
					
                </#list>
            </#if>

            <#if !isAlreadyInSaw>
                <#if !firstComment>,</#if>
            {
			<#if CommentcreatedAt??>
			"HPECommentcreatedAt": "${CommentcreatedAt}",
			"HPECommentcreatedAt2": "${CommentcreatedAt?replace(" ","")}",
			</#if>
			<#if commentTime??>
			"HPEcommentTime": "${commentTime}",
			</#if>
            "id": "${comment.id}",
            "type": "${comment.type}",
            <#if comment.completeDescription??>
            "description": "${comment.completeDescription}",
            <#else>
            "description": "${comment.description}",
            </#if>
            "isCustVisible": ${comment.isCustVisible?c},
             <#if comment.createdAt?is_number>
            "createdAt": "${comment.createdAt?number_to_datetime?string.long}",
            <#else>
            "createdAt": "${comment.createdAt}",
            </#if>
            "operator": "${comment.operator}"
			<#-- "commentTime" = "${cc.CommentBody?substring(1,cc.CommentBody?index_of("-"))?replace(" ","")?replace("(","")}" -->
			 }
                <#assign firstComment = false />
            </#if>
        </#list>
    </#if>
]
}

</#escape>