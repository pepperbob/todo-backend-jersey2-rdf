package de.byoc.todo;

import javax.inject.Singleton;

import jersey.repackaged.com.google.common.base.Throwables;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.byoc.todo.service.TodoItemService;
import de.byoc.todo.service.TodoItemServiceImpl;

public class TodoApplicationBinder extends AbstractBinder {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected void configure() {
		try {
			log.debug("Binding Services..");

			final Repository repository = new SailRepository(new MemoryStore());
			repository.initialize();
			
			bindFactory(new Factory<RepositoryConnection>() {
				@Override
				public RepositoryConnection provide() {
					try {
						log.debug("Opening Connection");
						return repository.getConnection();
					} catch (RepositoryException e) {
						throw Throwables.propagate(e);
					}
				}

				@Override
				public void dispose(RepositoryConnection instance) {
					try {
						// this gets never called for some reason; Jersey/HK2 issue
						log.debug("Disposing Connection {}", instance);
						instance.close();
					} catch (RepositoryException e) {
						throw Throwables.propagate(e);
					}
				}
			}).to(RepositoryConnection.class);

			bind(repository.getConnection()).to(RepositoryConnection.class);
			bind(repository).to(Repository.class);
			bind(ValueFactoryImpl.getInstance()).to(ValueFactory.class);
			bindAsContract(TodoItemServiceImpl.class).to(TodoItemService.class)
					.in(Singleton.class);
		} catch (RepositoryException e) {
			throw Throwables.propagate(e);
		}
	}

}
