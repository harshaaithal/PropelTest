package com.hp.ccue.serviceExchange.adapter.saw.freemarker;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelListSequence;
import freemarker.template.TemplateSequenceModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.hp.ccue.serviceExchange.operation.freemarker.FreeMarkerHelper.unwrapValue;

/**
 * Freemarker method for producing a sequence with removed duplicates.
 */
public class RemoveDuplicates implements TemplateMethodModelEx {

    /**
     * For a given sequence, the method returns a new sequence with duplicates removed.
     * @param arguments arguments with the following semantics:
     * <ul>
     * <li>List&lt;?&gt; list</li>
     * </ul>
     * @return the resulting sequence
     */
    @Override
    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public Object exec(List arguments) throws TemplateModelException {
        TemplateSequenceModel input = (TemplateSequenceModel) arguments.get(0);
        List<TemplateModel> resultList = new ArrayList<>();
        Set<Object> valueSet = new HashSet<>();
        final int inputSize = input.size();
        for (int i = 0; i != inputSize; ++i) {
            TemplateModel templateModel = input.get(i);
            Object value = unwrapValue(templateModel);
            if (valueSet.add(value)) {
                resultList.add(templateModel);
            }
        }
        return new TemplateModelListSequence(resultList);
    }

}
