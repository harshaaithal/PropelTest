package com.hp.ccue.serviceExchange.adapter.saw.util;

import com.hp.ccue.serviceExchange.adapter.util.SXAbstractProperties;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SXSAWImplProperties extends SXAbstractProperties {

    static private String baseFile="com.hp.ccue.serviceExchange.sawMessages";

    @Override
    public Object exec(List list) throws TemplateModelException {
        return execInternal(baseFile,list);
    }

    public static ResourceBundle getMessagesBundle(Locale l) {
        return getMessagesBundleInternal(baseFile,l);
    }

    public static ResourceBundle getMessagesBundle() {
        return getMessagesBundleInternal(baseFile);
    }

    public static String getMessage(Locale l,String key, Object ... arguments) {
        return getMessageInternal(baseFile,l,key, arguments);
    }

    public static String getMessage(String key, Object ... arguments) {
        return getMessageInternal(baseFile,key,arguments);
    }
}
