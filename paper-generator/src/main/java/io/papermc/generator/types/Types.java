package io.papermc.generator.types;

import com.squareup.javapoet.ClassName;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class Types {

    public static final String BASE_PACKAGE = "org.bukkit.craftbukkit";

    public static final ClassName CRAFT_BLOCK_DATA = ClassName.get(BASE_PACKAGE + ".block.data", "CraftBlockData");

    public static final ClassName CRAFT_BLOCK = ClassName.get(BASE_PACKAGE + ".block", "CraftBlock");
}
