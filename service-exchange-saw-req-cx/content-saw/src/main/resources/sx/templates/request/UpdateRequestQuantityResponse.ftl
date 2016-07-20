<#assign loadConfig='com.hp.ccue.serviceExchange.adapter.freemarker.LoadConfig'?new()/>
<#assign sawMapping=loadConfig(context.contentStorage, "saw-case-exchange/saw-mappings") />
<#assign findKey='com.hp.ccue.serviceExchange.adapter.freemarker.FindKeyForValue'?new()/>

{
  "tmp": {
        "sawQuantityEntityId": "${doc.result.entity_result_list[0].entity.properties.Id}"
  }
}