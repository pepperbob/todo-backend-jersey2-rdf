package de.byoc.todo.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import jersey.repackaged.com.google.common.base.Throwables;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
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
	public void createItem(TodoItem item) {
		RepositoryConnection con = provider.get();
		try {
			URI itemUri = vf.createURI(Todo.NS, item.getId());

			con.add(itemUri, RDF.TYPE, Todo.TYPE);
			con.add(itemUri, Todo.ID, vf.createLiteral(item.getId()));
			con.add(itemUri, Todo.TITLE, vf.createLiteral(item.getTitle()));
			
			boolean isCompleted = item.getCompleted() != null ? item.getCompleted() : false;
			con.add(itemUri, Todo.COMPLETED, vf.createLiteral(isCompleted));

			con.commit();
		} catch (RepositoryException e) {
			rollback(con);
			throw new RuntimeException(e);
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
		try {
			List<TodoItem> result = new ArrayList<>();

			RepositoryConnection con = provider.get();
			RepositoryResult<Statement> statements = con.getStatements(null,
					RDF.TYPE, Todo.TYPE, true);
			while (statements.hasNext()) {
				Statement next = statements.next();
				log.debug("Found: {}", next);
				String qs = "PREFIX : <http://todo.byoc.de/> SELECT ?id ?title ?completed WHERE { ?i :id ?id; :title ?title; :completed ?completed . FILTER (?i = <"
						+ next.getSubject() + ">) }";
				log.debug("Querying: {}", qs);
				TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, qs);
				TupleQueryResult rdfItem = q.evaluate();
				while (rdfItem.hasNext()) {
					TodoItem found = new TodoItem();
					BindingSet set = rdfItem.next();
					found.setId(set.getBinding("id").getValue().stringValue());
					found.setTitle(set.getBinding("title").getValue()
							.stringValue());
					found.setCompleted(set.getBinding("completed").getValue()
							.stringValue().equals("true"));
					log.debug("Adding {} to ResultSet", found);
					result.add(found);
				}
			}
			return result;
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void deleteAllItems() {
		try {
			RepositoryConnection con = provider.get();
			con.remove(con.getStatements(null, RDF.TYPE, Todo.TYPE, true));
			con.commit();
		} catch (RepositoryException e) {
			throw Throwables.propagate(e);
		}
	}

}
