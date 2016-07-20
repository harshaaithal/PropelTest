package com.hp.ccue.serviceExchange.adapter.saw.freemarker;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hp.ccue.serviceExchange.operation.freemarker.FreeMarkerHelper.unwrapValue;

/**
 * Freemarker method for generating ID for storing comments to SAW.
 * See UtilService#generateUID angular module (util-prdr.js)
 * <code><pre>
 * UtilService.generateUID(currentUser.Id).replace(/-/g, '');
 *
 * utils.generateUID = function (identifyNumber) {
 *   var d, xuuid, uuid;
 *   d = new Date().getTime();
 *   xuuid = 'xxxxxxxx-xxxx-' + identifyNumber + 'xxx-yxxx-xxxxxxxxxxxx';
 *   uuid = xuuid.replace(/[xy]/g, function (c) {
 *     var r = (d + Math.random() * 16) % 16 | 0;
 *     d = Math.floor(d / 16);
 *     return (c === 'x' ? r : (r & 0x7 | 0x8)).toString(16);
 *   });
 *   return uuid;
 * };
 * </pre></code>
 */
public class CommentIdGen implements TemplateMethodModelEx {

    @Override
    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public Object exec(List arguments) throws TemplateModelException {
        // @parameter 1: User ID
        Integer identifyNumber = parseUserId(unwrapValue(arguments.get(0)));
        String uuid = generateUuid(identifyNumber);
        return uuid.replaceAll("-", "");
    }

    private String generateUuid(Integer identifyNumber) {
        long d = System.currentTimeMillis();
        String xuuid = "xxxxxxxx-xxxx-" + identifyNumber + "xxx-yxxx-xxxxxxxxxxxx";
        Pattern p = Pattern.compile("[xy]");
        Matcher m = p.matcher(xuuid);
        StringBuffer uuid = new StringBuffer();
        while (m.find()) {
            String found = m.group();
            int r = (int)Math.floor((d + Math.random() * 16) % 16);
            d = (long)Math.floor(d / 16);
            String replacement = Integer.toHexString(found.equals("x") ? r : r & 0x7 | 0x8);
            m.appendReplacement(uuid, replacement);
        }
        m.appendTail(uuid);
        return uuid.toString();
    }

    private int parseUserId(Object arg) {
        if (arg instanceof String) {
            return Integer.parseInt((String)arg);
        } else if (arg instanceof Number) {
            return ((Number)arg).intValue();
        } else {
            throw new IllegalArgumentException("Unsupported input type " + arg.getClass().getName());
        }
    }

}
