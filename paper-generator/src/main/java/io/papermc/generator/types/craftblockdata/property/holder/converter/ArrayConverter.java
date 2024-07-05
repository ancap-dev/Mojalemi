package io.papermc.generator.types.craftblockdata.property.holder.converter;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.papermc.generator.types.craftblockdata.property.EnumPropertyWriter;
import io.papermc.generator.types.craftblockdata.property.converter.ConverterBase;
import io.papermc.generator.types.craftblockdata.property.holder.DataHolderType;

public class ArrayConverter implements DataConverter {

    @Override
    public DataHolderType getType() {
        return DataHolderType.ARRAY;
    }

    @Override
    public void convertSetter(final ConverterBase childConverter, final MethodSpec.Builder method, final FieldSpec field, final ParameterSpec indexParameter, final ParameterSpec parameter) {
        method.addStatement(childConverter.rawSetExprent().formatted("$N[$N]"), field, indexParameter, parameter);
    }

    @Override
    public void convertGetter(final ConverterBase childConverter, final MethodSpec.Builder method, final FieldSpec field, final ParameterSpec indexParameter) {
        if (childConverter instanceof EnumPropertyWriter<?> enumConverter) {
            method.addStatement("return " + childConverter.rawGetExprent().formatted("$N[$N]"), field, indexParameter, enumConverter.getApiType());
        } else {
            method.addStatement("return " + childConverter.rawGetExprent().formatted("$N[$N]"), field, indexParameter);
        }
    }
}
