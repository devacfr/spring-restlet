package org.cfr.restlet.ext.shindig.dashboard.resource;

import org.cfr.restlet.ext.shindig.dashboard.resource.impl.AddGadgetHandlerImpl;
import org.restlet.Response;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.representations.GadgetRepresentation;

/**
 * Interface for adding a gadget spec URI to a dashboard.
 */
@ImplementedBy(AddGadgetHandlerImpl.class)
public interface IAddGadgetHandler {

    /**
     * Adds the specified gadget to the specified dashboard.
     * @param dashboardId the ID of the dashboard to add to
     * @param gadgetRequestContext the context of this request
     * @param gadgetUrl the URL to the gadget spec
     */
    GadgetRepresentation addGadget(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, String gadgetUrl, Response response);

    /**
     * Adds the specified gadget to the specified dashboard in the specified column.
     * @param dashboardId the ID of the dashboard to add to
     * @param gadgetRequestContext the context of this request
     * @param gadgetUrl the URL to the gadget spec
     * @param columnIndex the column index to add the gadget to
     */
    GadgetRepresentation addGadget(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, String gadgetUrl,
            DashboardState.ColumnIndex columnIndex, Response response);

    /**
     * Moves the gadget specified by id from the source dashboard to the target dashboard.
     *
     * @param targetDashboardId The dashboard to move the gadget to
     * @param gadgetId The gadget to move
     * @param sourceDashboardId The source dashboard where the gadget will be deleted
     * @param columnIndex The column to add the gadget to
     * @param rowIndexAsInt The row to insert the gadget in
     * @param gadgetRequestContext the context of this request
     */
    GadgetRepresentation moveGadget(DashboardId targetDashboardId, GadgetId gadgetId, DashboardId sourceDashboardId,
            DashboardState.ColumnIndex columnIndex, int rowIndexAsInt, GadgetRequestContext gadgetRequestContext, Response response);
}
