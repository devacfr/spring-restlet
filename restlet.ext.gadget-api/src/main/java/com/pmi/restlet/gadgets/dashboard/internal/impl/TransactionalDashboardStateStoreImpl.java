package com.pmi.restlet.gadgets.dashboard.internal.impl;

import static org.cfr.commons.util.Assert.notNull;

import org.cfr.commons.sal.transaction.ITransactionCallback;
import org.cfr.commons.sal.transaction.ITransactionTemplate;

import com.google.inject.name.Named;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardNotFoundException;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.spi.DashboardStateStoreException;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardStateStore;
import com.pmi.restlet.gadgets.dashboard.spi.changes.IDashboardChange;

/**
 * A {@code DashboardStateStore} implementation that adds a transactional
 * wrapper around an existing {@code DashboardStateStore}
 */
public class TransactionalDashboardStateStoreImpl implements IDashboardStateStore {

	private final IDashboardStateStore stateStore;

	private final ITransactionTemplate<DashboardState> txTemplate;

	/**
	 * Constructor.
	 * 
	 * @param stateStore
	 *            the dashboard state store to use
	 * @param txTemplate
	 *            the transaction wrapper for persistence operations
	 */
	public TransactionalDashboardStateStoreImpl(@Named("shindig.state.store") IDashboardStateStore stateStore,
			ITransactionTemplate<DashboardState> txTemplate) {
		this.stateStore = notNull(stateStore);
		this.txTemplate = notNull(txTemplate);
	}

	@Override
	public DashboardState retrieve(final DashboardId dashboardId) throws DashboardNotFoundException {
		notNull(dashboardId, "dashboardId");
		return txTemplate.execute(new ITransactionCallback<DashboardState>() {

			@Override
			public DashboardState doInTransaction() {
				return stateStore.retrieve(dashboardId);
			}
		});
	}

	@Override
	public DashboardState update(final DashboardState state, final Iterable<IDashboardChange> changes)
			throws DashboardStateStoreException {
		notNull(state, "state");
		notNull(changes, "changes");
		return txTemplate.execute(new ITransactionCallback<DashboardState>() {

			@Override
			public DashboardState doInTransaction() {
				return stateStore.update(state, changes);
			}
		});
	}

	@Override
	public void remove(final DashboardId dashboardId) throws DashboardStateStoreException {
		notNull(dashboardId, "dashboardId");
		txTemplate.execute(new ITransactionCallback<DashboardState>() {

			@Override
			public DashboardState doInTransaction() {
				stateStore.remove(dashboardId);
				return null;
			}
		});
	}

	@Override
	public DashboardState findDashboardWithGadget(final GadgetId gadgetId) throws DashboardNotFoundException {
		notNull(gadgetId, "gadgetId");
		return txTemplate.execute(new ITransactionCallback<DashboardState>() {

			@Override
			public DashboardState doInTransaction() {
				return stateStore.findDashboardWithGadget(gadgetId);
			}
		});
	}
}
