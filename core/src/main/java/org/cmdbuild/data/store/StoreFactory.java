package org.cmdbuild.data.store;

public interface StoreFactory<T extends Storable> {

	Store<T> create(Groupable groupable);

}
