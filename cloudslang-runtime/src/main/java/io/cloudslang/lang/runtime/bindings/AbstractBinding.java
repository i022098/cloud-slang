package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapLoopStatement;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.python.core.PyObject;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: bancl
 * Date: 8/12/2016
 */
public class AbstractBinding {

    protected void validateStringValue(String errorMessagePrefix, Value value) {
        if (value != null && value.get() != null && !(value.get() instanceof String)) {
            throw new RuntimeException(errorMessagePrefix + "' should have a String value.");
        }
    }

    protected Value getEvalResultForMap(Value evalResult, LoopStatement loopStatement, String collectionExpression) {
        if (loopStatement instanceof MapLoopStatement) {
            if (evalResult != null && evalResult.get() instanceof Map) {
                List<Value> entriesAsValues = new ArrayList<>();
                @SuppressWarnings("unchecked") Set<Map.Entry<Serializable, Serializable>> entrySet = ((Map) evalResult.get()).entrySet();
                for (Map.Entry<Serializable, Serializable> entry : entrySet) {
                    entriesAsValues.add(ValueFactory.create(Pair.of(
                            ValueFactory.create(entry.getKey(), evalResult.isSensitive()),
                            ValueFactory.create(entry.getValue(), evalResult.isSensitive()))));
                }
                evalResult = ValueFactory.create((Serializable) entriesAsValues);
            } else {
                throw new RuntimeException(LoopsBinding.INVALID_MAP_EXPRESSION_MESSAGE + ": " + collectionExpression);
            }
        }
        return evalResult;
    }

    protected Iterable<Value> getIterableFromEvalResult(Value loopCollection) {
        Serializable loopCollectionContent = loopCollection.get();
        if (loopCollectionContent instanceof String) {
            String[] strings = ((String) loopCollectionContent).split(Pattern.quote(","));
            return convert(Arrays.asList(strings), loopCollection.isSensitive());
        } else if (loopCollectionContent instanceof PyObject) {
            PyObject pyObject = (PyObject) loopCollectionContent;
            return convert(pyObject.asIterable(), loopCollection.isSensitive());
        } if (loopCollectionContent instanceof Iterable) {
            //noinspection unchecked
            return (Iterable<Value>) loopCollectionContent;
        } else {
            return null;
        }
    }

    private Iterable<Value> convert(Iterable<? extends Serializable> iterable, boolean sensitive) {
        List<Value> values = new ArrayList<>();
        for (Serializable serializable : iterable) {
            values.add(ValueFactory.create(serializable, sensitive));
        }
        return values;
    }
}
