package com.pmi.restlet.gadgets.dashboard.spi.changes;

public interface IDashboardChange {

    public static interface Visitor {

        public abstract void visit(AddGadgetChange addgadgetchange);

        public abstract void visit(GadgetColorChange gadgetcolorchange);

        public abstract void visit(RemoveGadgetChange removegadgetchange);

        public abstract void visit(UpdateGadgetUserPrefsChange updategadgetuserprefschange);

        public abstract void visit(UpdateLayoutChange updatelayoutchange);
    }

    public abstract void accept(Visitor visitor);
}
