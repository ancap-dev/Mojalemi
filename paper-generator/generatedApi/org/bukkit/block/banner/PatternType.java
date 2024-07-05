package org.bukkit.block.banner;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum PatternType implements Keyed {
    // Paper start - Generated/PatternType
    // @GeneratedFrom 1.21
    BASE("b", "base"),
    BORDER("bo", "border"),
    BRICKS("bri", "bricks"),
    CIRCLE("mc", "circle"),
    CREEPER("cre", "creeper"),
    CROSS("cr", "cross"),
    CURLY_BORDER("cbo", "curly_border"),
    DIAGONAL_LEFT("ld", "diagonal_left"),
    DIAGONAL_RIGHT("rud", "diagonal_right"),
    DIAGONAL_UP_LEFT("lud", "diagonal_up_left"),
    DIAGONAL_UP_RIGHT("rd", "diagonal_up_right"),
    FLOW("flw", "flow"),
    FLOWER("flo", "flower"),
    GLOBE("glb", "globe"),
    GRADIENT("gra", "gradient"),
    GRADIENT_UP("gru", "gradient_up"),
    GUSTER("gus", "guster"),
    HALF_HORIZONTAL("hh", "half_horizontal"),
    HALF_HORIZONTAL_BOTTOM("hhb", "half_horizontal_bottom"),
    HALF_VERTICAL("vh", "half_vertical"),
    HALF_VERTICAL_RIGHT("vhr", "half_vertical_right"),
    MOJANG("moj", "mojang"),
    PIGLIN("pig", "piglin"),
    RHOMBUS("mr", "rhombus"),
    SKULL("sku", "skull"),
    SMALL_STRIPES("ss", "small_stripes"),
    SQUARE_BOTTOM_LEFT("bl", "square_bottom_left"),
    SQUARE_BOTTOM_RIGHT("br", "square_bottom_right"),
    SQUARE_TOP_LEFT("tl", "square_top_left"),
    SQUARE_TOP_RIGHT("tr", "square_top_right"),
    STRAIGHT_CROSS("sc", "straight_cross"),
    STRIPE_BOTTOM("bs", "stripe_bottom"),
    STRIPE_CENTER("cs", "stripe_center"),
    STRIPE_DOWNLEFT("dls", "stripe_downleft"),
    STRIPE_DOWNRIGHT("drs", "stripe_downright"),
    STRIPE_LEFT("ls", "stripe_left"),
    STRIPE_MIDDLE("ms", "stripe_middle"),
    STRIPE_RIGHT("rs", "stripe_right"),
    STRIPE_TOP("ts", "stripe_top"),
    TRIANGLE_BOTTOM("bt", "triangle_bottom"),
    TRIANGLE_TOP("tt", "triangle_top"),
    TRIANGLES_BOTTOM("bts", "triangles_bottom"),
    TRIANGLES_TOP("tts", "triangles_top");
    // Paper end - Generated/PatternType

    private final String identifier;
    private final NamespacedKey key;
    private static final Map<String, PatternType> byString = new HashMap<String, PatternType>();

    static {
        for (PatternType p : values()) {
            byString.put(p.identifier, p);
        }
    }

    private PatternType(/*@NotNull*/ String identifier, String key) {
        this.identifier = identifier;
        this.key = NamespacedKey.minecraft(key);
    }

    // Paper start - deprecate getKey
    /**
     * @deprecated use {@link Registry#getKey(Keyed)} and {@link Registry#BANNER_PATTERN}. PatternTypes
     * can exist without a key.
     */
    @Deprecated
    // Paper end - deprecate getKey
    @Override
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Returns the identifier used to represent
     * this pattern type
     *
     * @return the pattern's identifier
     * @see #getKey
     * @deprecated magic value
     */
    @NotNull
    @Deprecated(forRemoval = true)
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the pattern type which matches the passed
     * identifier or null if no matches are found
     *
     * @param identifier the identifier
     * @return the matched pattern type or null
     * @see Registry#BANNER_PATTERN
     * @deprecated magic value, use {@link Registry#get(NamespacedKey)} instead
     */
    @Contract("null -> null")
    @Nullable
    @Deprecated(forRemoval = true)
    public static PatternType getByIdentifier(@Nullable String identifier) {
        return byString.get(identifier);
    }
}
