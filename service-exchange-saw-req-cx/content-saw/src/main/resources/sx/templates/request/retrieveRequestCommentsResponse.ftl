<#assign writeJson='com.hp.ccue.serviceExchange.adapter.freemarker.WriteJson'?new()/>
<#-- @ftlvariable name="parseJson" type="com.hp.ccue.serviceExchange.adapter.freemarker.ParseJson" -->
<#assign parseJson='com.hp.ccue.serviceExchange.adapter.freemarker.ParseJson'?new()/>
<#escape x as x?json_string>
{
  "entityChange": {
    "entity": {
        "Comments": <#noescape>${writeJson(parseJson(doc.result.entities[0].properties.Comments!"{}"))}</#noescape>
    }
  }
}
</#escape>