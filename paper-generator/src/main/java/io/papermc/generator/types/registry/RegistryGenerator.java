package io.papermc.generator.types.registry;

import com.google.common.base.Suppliers;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.papermc.generator.Main;
import io.papermc.generator.types.SimpleGenerator;
import io.papermc.generator.utils.Annotations;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.Javadocs;
import io.papermc.generator.utils.RegistryUtils;
import io.papermc.generator.utils.experimental.FlagHolders;
import io.papermc.generator.utils.experimental.SingleFlagHolder;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;
import static io.papermc.generator.utils.Annotations.NOT_NULL;
import static java.util.Objects.requireNonNull;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public abstract class RegistryGenerator<T> extends SimpleGenerator {

    private final ClassName apiType;
    private final Registry<T> registry;
    private final RegistryKey<?> apiRegistryKey;
    private final boolean isInterface;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;

    protected RegistryGenerator(final String className, final String packageName, final ResourceKey<? extends Registry<T>> registryKey, final RegistryKey<?> apiRegistryKey, final boolean isInterface) {
        super(className, packageName);
        this.apiType = ClassName.get(packageName, className);
        this.registry = Main.REGISTRY_ACCESS.registryOrThrow(registryKey);
        this.apiRegistryKey = apiRegistryKey;
        this.isInterface = isInterface;
        this.experimentalKeys = Suppliers.memoize(() -> RegistryUtils.collectExperimentalDataDrivenKeys(this.registry));
        this.isFilteredRegistry = FeatureElement.FILTERED_REGISTRIES.contains(registryKey);
    }

    private MethodSpec.@Nullable Builder fetchMethod(TypeName returnType) {
        if (this.isInterface) {
            return null;
        }

        final TypeName keyType = TypeName.get(String.class).annotated(NOT_NULL);

        final ParameterSpec keyParam = ParameterSpec.builder(keyType, "key", FINAL).build();
        final MethodSpec.Builder fetch = MethodSpec.methodBuilder("fetch")
            .addModifiers(PRIVATE, STATIC)
            .addParameter(keyParam)
            .addCode("return $T.registryAccess().getRegistry($T.$L).get($T.minecraft($N));", RegistryAccess.class, RegistryKey.class, requireNonNull(RegistryUtils.REGISTRY_KEY_FIELD_NAMES.get(this.apiRegistryKey)), NamespacedKey.class, keyParam)
            .returns(returnType.annotated(NOT_NULL));
        return fetch;
    }

    private TypeSpec.Builder valueHolderType() {
        final TypeSpec.Builder typeBuilder;
        if (this.isInterface) {
            typeBuilder = interfaceBuilder(this.className);
        } else {
            typeBuilder = classBuilder(this.className).addModifiers(ABSTRACT); // legacy
        }

        return typeBuilder
            .addSuperinterface(Keyed.class)
            .addModifiers(PUBLIC)
            .addAnnotations(Annotations.CLASS_HEADER);
    }

    @Override
    protected TypeSpec getTypeSpec() {
        TypeSpec.Builder typeBuilder = this.valueHolderType();

        MethodSpec.@Nullable Builder fetchMethod = this.fetchMethod(this.apiType);

        String registryKeyField = requireNonNull(RegistryUtils.REGISTRY_KEY_FIELD_NAMES.get(this.apiRegistryKey));

        this.registry.holders().sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).forEach(reference -> {
            ResourceLocation key = reference.key().location();
            String pathKey = key.getPath();
            String fieldName = Formatting.formatKeyAsField(pathKey);

            FieldSpec.Builder fieldBuilder = FieldSpec.builder(this.apiType, fieldName, PUBLIC, STATIC, FINAL)
                .addJavadoc(Javadocs.getVersionDependentField("{@code $L}"), key.toString());
            if (this.isInterface) {
                fieldBuilder.initializer("$T.registryAccess().getRegistry($T.$L).get($T.minecraft($S))", RegistryAccess.class, RegistryKey.class, registryKeyField, NamespacedKey.class, pathKey);
            } else {
                fieldBuilder.initializer("$N($S)", fetchMethod.build(), pathKey);
            }
            @Nullable SingleFlagHolder requiredFeature = this.getRequiredFeature(reference);
            if (requiredFeature != null) {
                fieldBuilder.addAnnotations(Annotations.experimentalAnnotations(requiredFeature));
            }

            typeBuilder.addField(fieldBuilder.build());
        });

        if (fetchMethod != null) {
            typeBuilder.addMethod(fetchMethod.build());
        }

        this.addExtras(typeBuilder);

        return typeBuilder.build();
    }

    public abstract void addExtras(TypeSpec.Builder builder);

    public @Nullable SingleFlagHolder getRequiredFeature(Holder.Reference<T> reference) {
        if (this.isFilteredRegistry) {
            // built-in registry
            FeatureElement element = (FeatureElement) reference.value();
            if (FeatureFlags.isExperimental(element.requiredFeatures())) {
                return SingleFlagHolder.fromSet(element.requiredFeatures());
            }
        } else {
            // data-driven registry
            if (this.experimentalKeys.get().contains(reference.key())) {
                return FlagHolders.NEXT_UPDATE;
            }
        }
        return null;
    }
}
