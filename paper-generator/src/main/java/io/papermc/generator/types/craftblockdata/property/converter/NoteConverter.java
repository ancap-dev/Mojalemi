package io.papermc.generator.types.craftblockdata.property.converter;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Note;

public class NoteConverter implements Converter<Integer, Note> {

    @Override
    public Property<Integer> getProperty() {
        return BlockStateProperties.NOTE;
    }

    @Override
    public Class<Note> getApiType() {
        return Note.class;
    }

    @Override
    public String rawSetExprent() {
        return "this.set(%s, (int) $N.getId())";
    }

    @Override
    public void convertGetter(final MethodSpec.Builder method, final FieldSpec field) {
        method.addStatement("return " + this.rawGetExprent().formatted("$N"), this.getApiType(), field);
    }

    @Override
    public String rawGetExprent() {
        return "new $T(this.get(%s))";
    }
}
