package hellfirepvp.modularmachinery.common.util;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 一个控制器模式组，对应一个齿轮按钮；定义由同种机器共享，选中值由各控制器保存。 */
@ZenRegister
@ZenClass("mods.modularmachinery.ControllerMode")
public class ControllerMode {
    private final String name;
    private final int defaultValue;
    private final Map<Integer, String> modes = new LinkedHashMap<>();
    private boolean controllerButtonVisible;
    private String controllerButtonTooltip = "gui.controller.mode.switch";
    private String controllerButtonCurrentModeTooltip = "gui.controller.mode.current";

    private ControllerMode(String name, int defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @ZenMethod
    public static ControllerMode create(String name, int defaultValue) {
        return new ControllerMode(name, defaultValue);
    }

    @ZenMethod
    public ControllerMode addMode(int value, String displayName) {
        modes.put(value, displayName);
        return this;
    }

    @ZenGetter("name")
    public String getName() {
        return name;
    }

    @ZenGetter("defaultValue")
    public int getDefaultValue() {
        return defaultValue;
    }

    public Map<Integer, String> getModes() {
        return Collections.unmodifiableMap(modes);
    }

    @ZenGetter("controllerButtonVisible")
    public boolean isControllerButtonVisible() {
        return controllerButtonVisible;
    }

    @ZenMethod
    public ControllerMode setControllerButtonVisible(boolean visible) {
        controllerButtonVisible = visible;
        return this;
    }

    @ZenMethod
    public ControllerMode setControllerButtonTooltip(String instruction, String currentModeFormat) {
        controllerButtonTooltip = instruction;
        controllerButtonCurrentModeTooltip = currentModeFormat;
        return this;
    }

    public String getControllerButtonTooltip() {
        return controllerButtonTooltip;
    }

    public String getControllerButtonCurrentModeTooltip() {
        return controllerButtonCurrentModeTooltip;
    }
}
