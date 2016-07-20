<#assign writeJson='com.hp.ccue.serviceExchange.adapter.freemarker.WriteJson'?new()/>
<#escape x as x?json_string>
{
  <#if (doc.result.meta.total_count > 0)>
  "entityChange": {
    "entity": {
	"properties" : {
        "UAAssignedToId": <#noescape>${writeJson(doc.result.entities[0].properties.Id)}</#noescape>
	}
    }
  }
  <#else>
  "entityChange": {
    "entity": {
	"properties" : {
        "UAAssignedToId": ""
	}
    }
  }

  </#if>
}
</#escape>


