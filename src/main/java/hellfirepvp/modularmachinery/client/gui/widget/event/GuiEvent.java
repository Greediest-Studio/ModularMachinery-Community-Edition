package hellfirepvp.modularmachinery.client.gui.widget.event;

import hellfirepvp.modularmachinery.client.gui.widget.base.WidgetGui;

public abstract class GuiEvent {
    protected final WidgetGui gui;

    public GuiEvent(final WidgetGui gui) {
        this.gui = gui;
    }

    public WidgetGui getGui() {
        return gui;
    }
}
