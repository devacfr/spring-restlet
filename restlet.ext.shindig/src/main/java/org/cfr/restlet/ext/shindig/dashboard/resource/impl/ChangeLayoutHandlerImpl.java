package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.shindig.dashboard.resource.IChangeLayoutHandler;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetLayoutException;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.Layout;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.pmi.restlet.gadgets.dashboard.spi.GadgetLayout;

/**
 * Default implementation.
 */
@Singleton
public class ChangeLayoutHandlerImpl implements IChangeLayoutHandler {

	private final Logger log = LoggerFactory.getLogger(ChangeLayoutHandlerImpl.class);

	private final IDashboardRepository repository;

	private final I18nResolver i18n;

	@Inject
	public ChangeLayoutHandlerImpl(IDashboardRepository repository, I18nResolver i18n) {
		this.repository = repository;
		this.i18n = i18n;
	}

	@Override
	public void changeLayout(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, Response response,
			Form queryParams) {
		String layout = queryParams.getFirstValue("layout");
		IDashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
		try {
			if (StringUtils.isBlank(layout)) {
				rearrangeGadgets(dashboard, queryParams);
			} else {
				persistLayout(dashboard, response, queryParams);
			}
		} catch (GadgetLayoutException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("error.changing.dashboard.layout", e.getMessage()));
		} catch (ParseGadgetLayoutException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("error.changing.dashboard.layout", e.getMessage()));
		} catch (IOException ioe) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL,
					i18n.getText("error.changing.dashboard.layout", ioe.getMessage()));
		} catch (InconsistentDashboardStateException idse) {
			log.error("ChangeLayoutHandlerImpl: Unexpected error occurred", idse);
			response.setStatus(Status.CLIENT_ERROR_CONFLICT, i18n.getText(i18n.getText("error.please.reload")));
		}
	}

	private void rearrangeGadgets(IDashboard dashboard, Form queryParams) {
		GadgetLayout gadgetLayout = parseGadgetLayout(dashboard.getLayout(), queryParams);
		dashboard.rearrangeGadgets(gadgetLayout);
		repository.save(dashboard);
	}

	private void persistLayout(IDashboard dashboard, Response response, Form queryParams) throws IOException {
		Layout layout;
		String layoutParam = queryParams.getFirstValue("layout");
		try {
			layout = Layout.valueOf(layoutParam);
		} catch (IllegalArgumentException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("invalid.layout.parameter", layoutParam, Layout.values()));
			return;
		}
		GadgetLayout gadgetLayout = parseGadgetLayout(layout, queryParams);
		dashboard.changeLayout(layout, gadgetLayout);
		repository.save(dashboard);
	}

	private GadgetLayout parseGadgetLayout(Layout layout, Form queryParams) throws ParseGadgetLayoutException {
		List<Iterable<GadgetId>> columns = new ArrayList<Iterable<GadgetId>>(layout.getNumberOfColumns());
		columns.addAll(Collections.<Iterable<GadgetId>> nCopies(layout.getNumberOfColumns(),
				Collections.<GadgetId> emptyList()));
		for (int i = 0; i < columns.size(); i++) {
			String[] gadgetIds = queryParams.getValuesArray(Integer.toString(i));
			if (gadgetIds == null) {
				// there are no gadgets in this column
				continue;
			}
			try {
				columns.set(i, toGadgetIds(gadgetIds));
			} catch (NumberFormatException e) {
				throw new ParseGadgetLayoutException("gadget ids must be integers");
			}
		}
		return new GadgetLayout(columns);
	}

	private List<GadgetId> toGadgetIds(String[] gadgetIds) throws NumberFormatException {
		List<GadgetId> columnLayout = new LinkedList<GadgetId>();
		for (String gadgetId : gadgetIds) {
			columnLayout.add(GadgetId.valueOf(gadgetId));
		}
		return columnLayout;
	}

	private final class ParseGadgetLayoutException extends RuntimeException {

		/**
         * 
         */
		private static final long serialVersionUID = 3371861658708608625L;

		public ParseGadgetLayoutException(String message) {
			super(message);
		}
	}
}
