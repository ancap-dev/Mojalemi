package io.papermc.generator.types.registry;

import com.google.common.base.Suppliers;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
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
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import java.util.Set;
import java.util.function.Supplier;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlags;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static io.papermc.generator.utils.Annotations.EXPERIMENTAL_API_ANNOTATION;
import static io.papermc.generator.utils.Annotations.NOT_NULL;
import static io.papermc.generator.utils.Annotations.experimentalAnnotations;
import static java.util.Objects.requireNonNull;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class GeneratedKeyType<T, A> extends SimpleGenerator {

    private final Class<A> apiType;
    private final Registry<T> registry;
    private final RegistryKey<A> apiRegistryKey;
    private final boolean publicCreateKeyMethod;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;

    public GeneratedKeyType(final String className, final Class<A> apiType, final String packageName, final ResourceKey<? extends Registry<T>> registryKey, final RegistryKey<A> apiRegistryKey, final boolean publicCreateKeyMethod) {
        super(className, packageName);
        this.apiType = apiType;
        this.registry = Main.REGISTRY_ACCESS.registryOrThrow(registryKey);
        this.apiRegistryKey = apiRegistryKey;
        this.publicCreateKeyMethod = publicCreateKeyMethod;
        this.experimentalKeys = Suppliers.memoize(() -> RegistryUtils.collectExperimentalDataDrivenKeys(this.registry));
        this.isFilteredRegistry = FeatureElement.FILTERED_REGISTRIES.contains(registryKey);
    }

    private MethodSpec.Builder createMethod(final TypeName returnType) {
        final TypeName keyType = TypeName.get(Key.class).annotated(NOT_NULL);

        final ParameterSpec keyParam = ParameterSpec.builder(keyType, "key", FINAL).build();
        final MethodSpec.Builder create = MethodSpec.methodBuilder("create")
            .addModifiers(this.publicCreateKeyMethod ? PUBLIC : PRIVATE, STATIC)
            .addParameter(keyParam)
            .addCode("return $T.create($T.$L, $N);", TypedKey.class, RegistryKey.class, requireNonNull(RegistryUtils.REGISTRY_KEY_FIELD_NAMES.get(this.apiRegistryKey)), keyParam)
            .returns(returnType.annotated(NOT_NULL));
        if (this.publicCreateKeyMethod) {
            create.addAnnotation(EXPERIMENTAL_API_ANNOTATION); // TODO remove once not experimental
            create.addJavadoc(Javadocs.CREATE_TYPED_KEY_JAVADOC, this.apiType, this.registry.key().location().toString());
        }
        return create;
    }

    private TypeSpec.Builder keyHolderType() {
        return classBuilder(this.className)
            .addModifiers(PUBLIC, FINAL)
            .addJavadoc(Javadocs.getVersionDependentClassHeader("{@link $T#$L}"), RegistryKey.class, requireNonNull(RegistryUtils.REGISTRY_KEY_FIELD_NAMES.get(this.apiRegistryKey)))
            .addAnnotations(Annotations.CLASS_HEADER)
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(PRIVATE)
                .build()
            );
    }

    @Override
    protected TypeSpec getTypeSpec() {
        final TypeName typedKeyType = ParameterizedTypeName.get(TypedKey.class, this.apiType);

        final TypeSpec.Builder typeBuilder = this.keyHolderType();
        final MethodSpec.Builder createMethod = this.createMethod(typedKeyType);

        boolean allExperimental = true;
        for (final Holder.Reference<T> reference : this.registry.holders().sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).toList()) {
            final ResourceKey<T> key = reference.key();
            final String keyPath = key.location().getPath();
            final String fieldName = Formatting.formatKeyAsField(keyPath);
            final FieldSpec.Builder fieldBuilder = FieldSpec.builder(typedKeyType, fieldName, PUBLIC, STATIC, FINAL)
                .initializer("$N(key($S))", createMethod.build(), keyPath)
                .addJavadoc(Javadocs.getVersionDependentField("{@code $L}"), key.location().toString());

            final @Nullable SingleFlagHolder requiredFeature = this.getRequiredFeature(reference);
            if (requiredFeature != null) {
                fieldBuilder.addAnnotations(experimentalAnnotations(requiredFeature));
            } else {
                allExperimental = false;
            }
            typeBuilder.addField(fieldBuilder.build());
        }

        if (allExperimental) {
            typeBuilder.addAnnotation(EXPERIMENTAL_API_ANNOTATION);
            createMethod.addAnnotation(EXPERIMENTAL_API_ANNOTATION);
        } else {
            typeBuilder.addAnnotation(EXPERIMENTAL_API_ANNOTATION); // TODO experimental API
        }
        return typeBuilder.addMethod(createMethod.build()).build();
    }

    @Override
    protected JavaFile.Builder file(final JavaFile.Builder builder) {
        return builder.addStaticImport(Key.class, "key");
    }

    public @Nullable SingleFlagHolder getRequiredFeature(final Holder.Reference<T> reference) {
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
