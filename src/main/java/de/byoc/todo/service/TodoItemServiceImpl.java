package de.byoc.todo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import jersey.repackaged.com.google.common.base.Throwables;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.byoc.todo.data.Todo;
import de.byoc.todo.data.TodoItem;

public class TodoItemServiceImpl implements TodoItemService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Provider<RepositoryConnection> provider;
	private ValueFactory vf;

	@Inject
	public TodoItemServiceImpl(Provider<RepositoryConnection> provider,
			ValueFactory valueFactory) {
		this.provider = provider;
		this.vf = valueFactory;
	}

	@Override
	public TodoItem createItem(TodoItem item) {
		return createItem(item, Todo.NS);
	}

	@Override
	public TodoItem createItem(TodoItem item, String namespace) {
		RepositoryConnection con = provider.get();
		try {
			item.setId(UUID.randomUUID().toString());

			URI itemUri = vf.createURI(namespace, item.getId());

			con.add(itemUri, RDF.TYPE, Todo.TYPE);
			con.add(itemUri, Todo.ID, vf.createLiteral(item.getId()));
			con.add(itemUri, Todo.TITLE, vf.createLiteral(item.getTitle()));
			con.add(itemUri, Todo.URL, itemUri);

			boolean isCompleted = item.getCompleted() != null ? item
					.getCompleted() : false;
			con.add(itemUri, Todo.COMPLETED, vf.createLiteral(isCompleted));

			if (item.getOrder() != null) {
				con.add(itemUri, Todo.ORDER, vf.createLiteral(item.getOrder()));
			}

			con.commit();

			return getItem(item.getId());
		} catch (RepositoryException e) {
			rollback(con);
			throw Throwables.propagate(e);
		} finally {
			close(con);
		}
	}

	@Override
	public void updateItem(TodoItem item) {
		RepositoryConnection con = provider.get();
		try {
			URI existingUri = vf.createURI(item.getUrl());

			con.remove(con.getStatements(existingUri, Todo.TITLE, null, true));
			con.add(existingUri, Todo.TITLE, vf.createLiteral(item.getTitle()));

			boolean isCompleted = item.getCompleted() != null ? item
					.getCompleted() : false;
			con.remove(con.getStatements(existingUri, Todo.COMPLETED, null,
					true));
			con.add(existingUri, Todo.COMPLETED, vf.createLiteral(isCompleted));

			if (item.getOrder() != null) {
				con.remove(con.getStatements(existingUri, Todo.ORDER, null,
						true));
				con.add(existingUri, Todo.ORDER,
						vf.createLiteral(item.getOrder()));
			}

			con.commit();
		} catch (RepositoryException e) {
			rollback(con);
			throw Throwables.propagate(e);
		} finally {
			close(con);
		}
	}

	private void rollback(RepositoryConnection con) {
		try {
			con.rollback();
		} catch (RepositoryException e) {
			Throwables.propagate(e);
		}
	}

	@Override
	public List<TodoItem> getAllItems() {
		RepositoryConnection con = provider.get();
		try {
			List<TodoItem> result = new ArrayList<>();

			RepositoryResult<Statement> statements = con.getStatements(null,
					RDF.TYPE, Todo.TYPE, true);
			while (statements.hasNext()) {
				Statement next = statements.next();
				TodoItem item = findItem(next.getSubject());
				result.add(item);
			}
			return result;
		} catch (RepositoryException e) {
			throw Throwables.propagate(e);
		} finally {
			close(con);
		}
	}

	@Override
	public void deleteAllItems() {
		RepositoryConnection con = provider.get();
		try {
			con.remove(con.getStatements(null, RDF.TYPE, Todo.TYPE, true));
			con.commit();
		} catch (RepositoryException e) {
			throw Throwables.propagate(e);
		} finally {
			close(con);
		}
	}

	@Override
	public TodoItem getItem(String itemId) {
		RepositoryConnection con = provider.get();
		try {
			String qs = "PREFIX : <http://todo.byoc.de/> SELECT ?item WHERE { ?item :id ?id . FILTER (?id = '%s') }";
			log.debug("Query by itemId: {}", qs);
			TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL,
					String.format(qs, itemId));
			TupleQueryResult result = query.evaluate();
			if (result.hasNext()) {
				Value item = result.next().getBinding("item").getValue();
				return findItem(item);
			}
			return null;
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
			throw Throwables.propagate(e);
		} finally {
			close(con);
		}
	}

	private TodoItem findItem(Value item) {
		RepositoryConnection con = provider.get();
		try {
			log.debug("Getting item {}", item);
			String qs = "PREFIX : <http://todo.byoc.de/> SELECT ?i ?id ?title ?completed ?order WHERE { ?i :id ?id; :title ?title; :completed ?completed . OPTIONAL { ?i :order ?order } FILTER (?i = <%s>) }";
			log.debug("Querying: {}", qs);
			TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL,
					String.format(qs, item));
			TupleQueryResult rdfItem = q.evaluate();
			if (!rdfItem.hasNext()) {
				return null;
			}

			TodoItem found = new TodoItem();
			BindingSet set = rdfItem.next();
			found.setId(set.getBinding("id").getValue().stringValue());
			found.setTitle(set.getBinding("title").getValue().stringValue());
			found.setCompleted(set.getBinding("completed").getValue()
					.stringValue().equals("true"));
			found.setUrl(set.getBinding("i").getValue().stringValue());

			if (set.getBinding("order") != null) {
				found.setOrder(Integer.valueOf(set.getBinding("order")
						.getValue().stringValue()));
			}

			log.debug("Adding {} to ResultSet", found);
			return found;
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
			throw Throwables.propagate(e);
		} finally {
			close(con);
		}
	}

	@Override
	public void deleteItem(String itemId) {
		RepositoryConnection con = provider.get();
		try {
			TodoItem item = getItem(itemId);
			if (item == null) {
				return;
			}
			URI url = vf.createURI(item.getUrl());
			con.remove(con.getStatements(url, null, null, true));
			con.commit();
		} catch (RepositoryException e) {
			throw Throwables.propagate(e);
		} finally {
			close(con);
		}
	}

	private void close(RepositoryConnection con) {
		try {
			log.debug("Closing connection");
			if (con == null || !con.isOpen()) {
				return;
			}
			con.close();
		} catch (RepositoryException ignored) {
		}
	}

}
