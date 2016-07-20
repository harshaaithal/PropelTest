<#escape x as x?json_string>
{
    "args": {
        "linkedEntity": {
            "entityType": "Request",
            "entityId": "${message.args.linkedEntity.entityId}",
            "properties": {
                "Status": "Ready"
            }
        }
    }
}
</#escape>
