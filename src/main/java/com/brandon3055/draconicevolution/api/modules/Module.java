package com.brandon3055.draconicevolution.api.modules;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleEntity;
import com.brandon3055.draconicevolution.api.modules.data.ModuleData;
import com.brandon3055.draconicevolution.api.modules.data.ModuleProperties;
import com.brandon3055.draconicevolution.api.modules.lib.InstallResult;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;
import java.util.List;

/**
 * Created by brandon3055 and covers1624 on 4/16/20.
 */
public interface Module<T extends ModuleData<T>> extends IForgeRegistryEntry<Module<?>> {

    ModuleType<T> getType();

    ModuleProperties<T> getProperties();

    /**
     * @return a convenience method for getting the module data.
     */
    default T getData() {
        return getProperties().getData();
    }

    /**
     * This is just a convenience method. This should always return the same tech level as defined by the {@link ModuleProperties}
     */
    default TechLevel getModuleTechLevel() {
        return getProperties().getTechLevel();
    }

    Item getItem();

    default Collection<ModuleCategory> getCategories() {
        return getType().getCategories();
    }

    /**
     * Ideally the module entity should be always be created by the {@link ModuleType} because all modules of a specific type should use the same entity.
     * However if for some reason you wish need to modify the module entity for your module this method can be used to do that.<br><br>
     * <b>
     * Note the module entity you return MUST extend the default module entity for this module's type.
     * If you do not do this any code that needs to retrieve, cast and interact with this entity WILL break.</b>
     * In other words <br>
     * Module#getType().createEntity(this).getClass().isAssignableFrom(Module#createEntity().getClass())<br>
     * Must return true.
     * <br><br>
     *
     * @return a new {@link ModuleEntity} instance for this module.
     * @see ModuleType#createEntity(Module)
     */
    default ModuleEntity createEntity() {
        return getType().createEntity(this);
    }

    /**
     * This allows you to prevent this module from being installed along side any other specific module.
     *
     * @param otherModule Other module.
     * @return pass with null value if this module can coexist with the other module.
     * Otherwise return fail with an ITextTranslation specifying a reason that can be displayed to the player.
     */
    default InstallResult areModulesCompatible(Module<?> otherModule) {
        return getType().areModulesCompatible(this, otherModule);
    }

    /**
     * @return The maximum number of modules of this type that can be installed (-1 = no limit)
     */
    default int maxInstallable() {
        return getType().maxInstallable();
    }

    default void addInformation(List<ITextComponent> toolTip) {
        getProperties().addStats(toolTip, this);

        if (maxInstallable() != -1) {
            toolTip.add(new TranslationTextComponent("module.draconicevolution.max_installable") //
                    .withStyle(TextFormatting.GRAY) //
                    .append(": ") //
                    .append(new StringTextComponent(String.valueOf(maxInstallable())) //
                            .withStyle(TextFormatting.DARK_GREEN)));
        }
    }
}
