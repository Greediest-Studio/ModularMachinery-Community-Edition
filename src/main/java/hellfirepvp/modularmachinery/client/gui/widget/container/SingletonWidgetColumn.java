package hellfirepvp.modularmachinery.client.gui.widget.container;

import hellfirepvp.modularmachinery.client.gui.widget.base.DynamicWidget;

public class SingletonWidgetColumn extends Column {

    public SingletonWidgetColumn(final DynamicWidget widget) {
        super.addWidget(widget);
    }

    @Override
    public Column addWidget(final DynamicWidget widget) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Column removeWidget(final DynamicWidget widget) {
        throw new UnsupportedOperationException();
    }
}
