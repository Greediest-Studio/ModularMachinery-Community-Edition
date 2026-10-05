// 移植自 MMCE-AdvancedBuilderTool，按 GPL-3.0 许可融合。
package hellfirepvp.modularmachinery.common.integration.mmcecomplement;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import net.edwin.mmcecomplement.attachment.AttachmentMachine;
import net.edwin.mmcecomplement.attachment.AttachmentModule;

import java.util.Map;

/**
 * Bridge to MMCE Complement's attachment-module API.
 *
 * <p>The mod is a {@code compileOnly} dependency, so every entry point of this class
 * is guarded by {@link Mods#MMCE_COMPLEMENT}. Nothing here may run without that guard:
 * {@link #findPattern} touches {@link AttachmentMachine} types directly. The
 * MMCE Complement only handler lives in the nested class below so that loading this
 * class never drags those types in.
 */
public final class AttachmentModuleCompat {

    public static boolean isAvailable() {
        return Mods.MMCE_COMPLEMENT.isPresent() && Availability.COMPATIBLE;
    }

    private static final class Availability {
        private static final boolean COMPATIBLE = check();
        private static boolean check() {
            try {
                return AttachmentMachine.class.isAssignableFrom(DynamicMachine.class)
                    && AttachmentModule.class.getMethod("getEffectivePattern", TaggedPositionBlockArray.class, Map.class)
                        .getReturnType() == TaggedPositionBlockArray.class;
            } catch (ReflectiveOperationException | LinkageError error) {
                ModularMachinery.log.warn("MMCE Complement 附属模块 API 不兼容，已隐藏搭建入口", error);
                return false;
            }
        }
    }

    /**
     * Resolves the selected module-only pattern.
     *
     * @return the attachment pattern, or {@code null} when the id is empty, MMCE
     *         Complement is absent, or the machine has no such module
     */
    public static TaggedPositionBlockArray findPattern(DynamicMachine machine, String moduleId) {
        if (machine == null || moduleId == null || moduleId.trim().isEmpty()
                || !isAvailable()) {
            return null;
        }
        return Handler.findPattern(machine, moduleId.trim());
    }

    private static final class Handler {

        private Handler() {
        }

        private static TaggedPositionBlockArray findPattern(DynamicMachine machine, String moduleId) {
            try {
                if (!(machine instanceof AttachmentMachine)) {
                    return null;
                }

                Map<String, AttachmentModule> modules =
                        ((AttachmentMachine) machine).mmceComplement$getAttachmentModules();
                if (modules == null) {
                    return null;
                }

                AttachmentModule module = modules.get(moduleId);
                if (module == null) {
                    return null;
                }

                TaggedPositionBlockArray pattern =
                        module.getEffectivePattern(machine.getPattern(), modules);
                return pattern == null ? null : new TaggedPositionBlockArray(pattern);
            } catch (LinkageError error) {
                ModularMachinery.log.warn(
                        "MMCE Complement attachment-module API is incompatible with this build; "
                                + "the attachment cannot be assembled", error);
                return null;
            }
        }
    }
}
