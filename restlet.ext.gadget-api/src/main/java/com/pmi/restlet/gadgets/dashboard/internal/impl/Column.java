package com.pmi.restlet.gadgets.dashboard.internal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;

class Column {

    private final List<IGadget> gadgets = new ArrayList<IGadget>();

    Iterable<IGadget> getGadgets() {
        return gadgets;
    }

    void appendGadget(IGadget gadget) {
        gadgets.add(gadget);
    }

    void addGadget(IGadget gadget) {
        gadgets.add(0, gadget);
    }

    boolean containsGadget(GadgetId gadgetId) {
        for (IGadget gadget : gadgets) {
            if (gadget.getId().equals(gadgetId)) {
                return true;
            }
        }
        return false;
    }

    void removeGadget(GadgetId gadgetId) {
        for (Iterator<IGadget> it = gadgets.iterator(); it.hasNext();) {
            IGadget gadget = it.next();
            if (gadget.getId().equals(gadgetId)) {
                it.remove();
            }
        }
    }

    Map<GadgetId, IGadget> getGadgetMap() {
        Map<GadgetId, IGadget> gadgets = new HashMap<GadgetId, IGadget>();
        for (IGadget gadget : getGadgets()) {
            gadgets.put(gadget.getId(), gadget);
        }
        return gadgets;
    }

    void clear() {
        gadgets.clear();
    }
}
