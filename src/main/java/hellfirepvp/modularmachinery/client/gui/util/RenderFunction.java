package hellfirepvp.modularmachinery.client.gui.util;

import hellfirepvp.modularmachinery.client.gui.widget.base.DynamicWidget;
import hellfirepvp.modularmachinery.client.gui.widget.base.WidgetGui;

@FunctionalInterface
public interface RenderFunction {

    void doRender(DynamicWidget dynamicWidget, WidgetGui gui, RenderSize renderSize, RenderPos renderPos, MousePos mousePos);

}
