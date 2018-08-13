package com.jkoh.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.mongodb.*;

import org.jongo.*;
import org.jongo.marshall.jackson.JacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import lombok.Getter;

public class MongoDB {
	private final static Logger logger = LoggerFactory.getLogger(MongoDB.class);
	private Config config = null;
	private MongoClient mongoClient = null;
	private Jongo jongo = null;
	private Mapper mapper = null;

	public MongoDB (Config config) {
		this.config = config;
	}
	public boolean start () {
		if (mongoClient == null) {
			try {
				logger.info("Connecting to mongodb");
				List<ServerAddress> addresses = new ArrayList<>();
				for (Map.Entry<String, Integer> address : config.getServers().entrySet()) {
					addresses.add(new ServerAddress(address.getKey(), address.getValue()));
				}
				MongoCredential credential = MongoCredential.createScramSha1Credential(
					config.getUsername(), config.getAuthDB(), config.getPassword().toCharArray());
				List<MongoCredential> auth = Arrays.asList(credential);
				mongoClient = new MongoClient(addresses, auth);
				DB db = mongoClient.getDB(config.getDbName());
				db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
				jongo = new Jongo(db);
				mapper = new JacksonMapper.Builder()
					.addDeserializer(Date.class, new JsonUtil.DateDeserializer())
					.addSerializer(Date.class, new JsonUtil.DateSerializer())
					.setVisibilityChecker(VisibilityChecker.Std.defaultInstance()
						.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
						.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
						.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
						.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
						.withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
					.build();
				logger.info("Connected to mongodb");
				return true;
			} catch (MongoException e) {
				logger.warn("Failed to connect to mongodb", e);
			}
		} else {
			logger.info("MongoDB already started");
		}
		return false;
	}
	public void stop () {
		logger.info("Disconnecting from mongodb");
		if (mongoClient != null) {
			mongoClient.close();
		}
		jongo = null;
		mongoClient = null;
		config = null;
		logger.info("Disconnected from mongodb");
	}

	/** Check if a collection exists */
	public <T> boolean collectionExists (Class<T> clazz) {
		return collectionExists(clazz.getSimpleName());
	}
	/** Check if a collection exists */
	public boolean collectionExists (String collectionName) {
		return jongo.getDatabase().collectionExists(collectionName);
	}

	/** Create collection */
	public <T> void createCollection (Class<T> clazz) {
		createCollection(clazz.getSimpleName());
	}
	/** Create collection */
	public void createCollection (String collectionName) {
		jongo.getDatabase().createCollection(collectionName, new BasicDBObject());
	}

	/** Ensure a indexed field on a collection */
	public <T> void index (Class<T> clazz, String index) {
		index(clazz.getSimpleName(), index);
	}
	/** Ensure a indexed field on a collection */
	public void index (String collectionName, String index) {
		index(collectionName, index, true);
	}
	/** Ensure a indexed field on a collection */
	public <T> void index (Class<T> clazz, String index, boolean background) {
		index(clazz.getSimpleName(), index, background);
	}
	/** Ensure a indexed field on a collection */
	public void index (String collectionName, String index, boolean background) {
		index(collectionName, index, background ? "{background:true}" : "{}");
	}
	/** Ensure a indexed field on a collection */
	public <T> void index (Class<T> clazz, String index, String options) {
		index(clazz.getSimpleName(), index, options);
	}
	/** Ensure a indexed field on a collection */
	public void index (String collectionName, String index, String options) {
		try {
			jongo.getCollection(collectionName).ensureIndex(index, options);
		} catch (MongoException e) {
			logger.warn("Index failed", e);
		}
	}

	/** Check if a specific document exists */
	public <T> boolean exists (Class<T> clazz, Query query) {
		return exists(clazz.getSimpleName(), query);
	}
	/** Check if a specific document exists */
	public <T> boolean exists (String collectionName, Query query) {
		return count(collectionName, query) > 0;
	}

	/** Count documents */
	public <T> long count (Class<T> clazz, Query query) {
		return count(clazz.getSimpleName(), query);
	}
	/** Count documents */
	public long count (String collectionName, Query query) {
		try {
			return jongo.getCollection(collectionName).count(query.query, query.params);
		} catch (MongoException e) {
			logger.warn("Count failed", e);
			return 0;
		}
	}
	
	/** Count position of a document by id in the list of results */
	public <T> long countPosition (Class<T> clazz, String id, Query query) {
		return countPosition(clazz.getSimpleName(), id, query);
	}
	/** Count position of a document by id in the list of results */
	public long countPosition (String collectionName, String id, Query query) {
		return countPosition(collectionName, id, query, null);
	}
	/** Count position of a document by id in the list of results */
	public <T> long countPosition (Class<T> clazz, String id, Query query, String sort) {
		return countPosition(clazz.getSimpleName(), id, query, sort);
	}
	/** Count position of a document by id in the list of results */
	public long countPosition (String collectionName, String id, Query query, String sort) {
		MongoCursor<Map> cursor = cursorFind(collectionName, query, Map.class, 
			findOptions().projection(idQuery(1)).sort(sort));
		boolean found = false;
		long position = 0;
		while(cursor.hasNext()) {
			if(cursor.next().get("_id").equals(id)) {
				found = true;
				break;
			}
			position++;
		}
		try {
			cursor.close();
		} catch (IOException e) {}
		return found ? position : -1;
	}
	
	/** Count documents returned by find */
	public <T> int countFindResults (Class<T> clazz, Query query) {
		return countFindResults(clazz.getSimpleName(), query);
	}
	/** Count documents returned by find  */
	public int countFindResults (String collectionName, Query query) {
		return countFindResults(collectionName, query, null);
	}
	/** Count documents returned by find  */
	public <T> int countFindResults (Class<T> clazz, Query query, FindOptions options) {
		return countFindResults(clazz.getSimpleName(), query, options);
	}
	/** Count documents returned by find  */
	public int countFindResults (String collectionName, Query query, FindOptions options) {
		MongoCursor<?> cursor = cursorFind(collectionName, query, Object.class, options);
		int count = cursor.count();
		try {
			cursor.close();
		} catch (IOException e) {}
		return count;
	}
	
	
	/** Get a document by id */
	public <T> T get (Class<T> clazz, String id) {
		return get(clazz.getSimpleName(), id, clazz);
	}
	/** Get a document by id */
	public <T> T get (String collectionName, String id, Class<T> clazz) {
		return findOne(collectionName, idQuery(id), clazz);
	}
	/** Find a document */
	public <T> T findOne (Class<T> clazz, Query query) {
		return findOne(clazz.getSimpleName(), query, clazz);
	}
	/** Find a document */
	public <T> T findOne (String collectionName, Query query, Class<T> clazz) {
		return findOne(collectionName, query, clazz, null);
	}
	/** Find a document */
	public <T> T findOne (Class<T> clazz, Query query, FindOneOptions options) {
		return findOne(clazz.getSimpleName(), query, clazz, options);
	}
	/** Find a document */
	public <T> T findOne (String collectionName, Query query, Class<T> clazz, FindOneOptions options) {
		try {
			FindOne findOne = jongo.getCollection(collectionName).findOne(query.query, query.params);
			if(options != null) {
				if(options.projection != null) {
					findOne.projection(options.projection.query, options.projection.params);
				}
				if(options.orderBy != null) {
					findOne.orderBy(options.orderBy);
				}
			}
			return findOne.as(clazz);
		} catch (MongoException e) {
			logger.warn("FindOne failed", e);
			return null;
		}
	}

	/** Find documents */
	public <T> List<T> find (Class<T> clazz, Query query) {
		return find(clazz.getSimpleName(), query, clazz);
	}
	/** Find documents */
	public <T> List<T> find (String collectionName, Query query, Class<T> clazz) {
		return find(collectionName, query, clazz, null);
	}
	/** Find documents */
	public <T> List<T> find (Class<T> clazz, Query query, FindOptions options) {
		return find(clazz.getSimpleName(), query, clazz, options);
	}
	/** Find documents */
	public <T> List<T> find (String collectionName, Query query, Class<T> clazz, FindOptions options) {
		List<T> results = new ArrayList<>();
		MongoCursor<T> cursor = cursorFind(collectionName, query, clazz, options);
		while(cursor.hasNext()) {
			results.add(cursor.next());
		}
		try {
			cursor.close();
		} catch (IOException e) {}
		return results;
	}
	/** Find documents and return the cursor */
	public <T> MongoCursor<T> cursorFind (Class<T> clazz, Query query) {
		return cursorFind(clazz.getSimpleName(), query, clazz);
	}
	/** Find documents and return the cursor */
	public <T> MongoCursor<T> cursorFind (String collectionName, Query query, Class<T> clazz) {
		return cursorFind(collectionName, query, clazz, null);
	}
	/** Find documents and return the cursor */
	public <T> MongoCursor<T> cursorFind (Class<T> clazz, Query query, FindOptions options) {
		return cursorFind(clazz.getSimpleName(), query, clazz, options);
	}
	/** Find documents and return the cursor */
	public <T> MongoCursor<T> cursorFind (String collectionName, Query query, Class<T> clazz, FindOptions options) {
		try {
			Find find = jongo.getCollection(collectionName).find(query.query, query.params);
			if(options != null) {
				if(options.projection != null) {
					find.projection(options.projection.query, options.projection.params);
				}
				if(options.limit > 0) {
					find.limit(options.limit);
				}
				if(options.skip > 0) {
					find.skip(options.skip);
				}
				if(options.sort != null) {
					find.sort(options.sort);
				}
				if(options.hint != null) {
					find.hint(options.hint);
				}
			}
			return find.as(clazz);
		} catch (MongoException e) {
			logger.warn("Find failed", e);
			return null;
		}
	}

	/** Find and modify one document */
	public <T> T findAndModify (Class<T> clazz, Query query, Query modifier) {
		return findAndModify(clazz.getSimpleName(), query, modifier, clazz);
	}
	/** Find and modify one document */
	public <T> T findAndModify (String collectionName, Query query, Query modifier, Class<T> clazz) {
		return findAndModify(collectionName, query, modifier, clazz, null);
	}
	/** Find and modify one document */
	public <T> T findAndModify (Class<T> clazz, Query query, Query modifier, FindAndModifyOptions options) {
		return findAndModify(clazz.getSimpleName(), query, modifier, clazz, options);
	}
	/** Find and modify one document */
	public <T> T findAndModify (String collectionName, Query query, Query modifier, Class<T> clazz, FindAndModifyOptions options) {
		try {
			FindAndModify findAndModify = jongo.getCollection(collectionName).findAndModify(query.query, query.params)
				.with(modifier.query, modifier.params);
			if(options != null) {
				if(options.projection != null) {
					findAndModify.projection(options.projection.query, options.projection.params);
				}
				if(options.sort != null) {
					findAndModify.sort(options.sort);
				}
				if(options.returnNew) {
					findAndModify.returnNew();
				}
				if(options.upsert) {
					findAndModify.upsert();
				}
			}
			return findAndModify.as(clazz);
		} catch (MongoException e) {
			logger.warn("FindAndModify failed", e);
			return null;
		}
	}

	/** Find and remove one document */
	public <T> T findAndRemove (Class<T> clazz, Query query) {
		return findAndRemove(clazz.getSimpleName(), query, clazz);
	}
	/** Find and remove one document */
	public <T> T findAndRemove (String collectionName, Query query, Class<T> clazz) {
		return findAndRemove(collectionName, query, clazz, null);
	}
	/** Find and remove one document */
	public <T> T findAndRemove (Class<T> clazz, Query query, FindAndRemoveOptions options) {
		return findAndRemove(clazz.getSimpleName(), query, clazz, options);
	}
	/** Find and remove one document */
	public <T> T findAndRemove (String collectionName, Query query, Class<T> clazz, FindAndRemoveOptions options) {
		try {
			FindAndModify findAndRemove = jongo.getCollection(collectionName).findAndModify(query.query, query.params)
				.remove();
			if(options != null) {
				if(options.projection != null) {
					findAndRemove.projection(options.projection.query, options.projection.params);
				}
				if(options.sort != null) {
					findAndRemove.sort(options.sort);
				}
			}
			return findAndRemove.as(clazz);
		} catch (MongoException e) {
			logger.warn("FindAndRemove failed", e);
			return null;
		}
	}

	/** Upsert */
	public <T> boolean set (T object) {
		return set(object.getClass().getSimpleName(), object);
	}
	/** Upsert */
	public <T> boolean set (String collectionName, T object) {
		try {
			return jongo.getCollection(collectionName).save(object).getN() > 0;
		} catch (MongoException e) {
			logger.warn("Set failed", e);
			return false;
		}
	}
	/** Bulk upsert */
	public <T> int bulkSet (Class<T> clazz, List<T> objects) {
		return bulkSet(clazz.getSimpleName(), objects);
	}
	/** Bulk upsert */
	public <T> int bulkSet (String collectionName, List<T> objects) {
		try {
			BulkWriteOperation bulkWriteOperation = jongo.getDatabase().getCollection(collectionName).initializeUnorderedBulkOperation();
			for (T object : objects) {
				DBObject dbObject = mapper.getMarshaller().marshall(object).toDBObject();
				bulkWriteOperation.find(new BasicDBObject("_id", dbObject.get("_id"))).upsert().replaceOne(dbObject);
			}
			BulkWriteResult result = bulkWriteOperation.execute();
			return result.getUpserts().size();
		} catch (MongoException e) {
			logger.warn("BulkSet failed", e);
			return 0;
		}
	}
	
	/** Insert */
	public <T> boolean insert (Class<T> clazz, Query query) {
		return insert(clazz.getSimpleName(), query);
	}
	/** Insert */
	public <T> boolean insert (String collectionName, Query query) {
		try {
			jongo.getCollection(collectionName).insert(query.query, query.params);
			return true;
		} catch (DuplicateKeyException e) {
			return false;
		} catch (MongoException e) {
			logger.warn("Insert failed", e);
			return false;
		}
	}
	/** Insert */
	public <T> boolean insert (T object) {
		return insert(object.getClass().getSimpleName(), object);
	}
	/** Insert */
	public <T> boolean insert (String collectionName, T object) {
		try {
			jongo.getCollection(collectionName).insert(object);
			return true;
		} catch (DuplicateKeyException e) {
			return false;
		} catch (MongoException e) {
			logger.warn("Insert failed", e);
			return false;
		}
	}
	
	/** Update documents */
	public <T> int update (Class<T> clazz, Query query, Query modifier) {
		return update(clazz.getSimpleName(), query, modifier);
	}
	/** Update documents */
	public int update (String collectionName, Query query, Query modifier) {
		return update(collectionName, query, modifier, null);
	}
	/** Update documents */
	public <T> int update (Class<T> clazz, Query query, Query modifier, UpdateOptions options) {
		return update(clazz.getSimpleName(), query, modifier, options);
	}
	/** Update documents */
	public int update (String collectionName, Query query, Query modifier, UpdateOptions options) {
		try {
			Update update = jongo.getCollection(collectionName).update(query.query, query.params);
			if(options != null) {
				if(options.upsert) {
					update.upsert();
				}
				if(options.multi) {
					update.multi();
				}
			}
			return update.with(modifier.query, modifier.params).getN();
		} catch (MongoException e) {
			logger.warn("Update failed", e);
			return 0;
		}
	}

	/** Remove documents */
	public <T> int remove (Class<T> clazz, Query query) {
		return remove(clazz.getSimpleName(), query);
	}
	/** Remove documents */
	public int remove (String collectionName, Query query) {
		try {
			return jongo.getCollection(collectionName).remove(query.query, query.params).getN();
		} catch (MongoException e) {
			logger.warn("Remove failed", e);
			return 0;
		}
	}
	
	/** Get distinct values for a key */
	public List<Object> distinct (String collectionName, String key) {
		return distinct(collectionName, key, null);
	}
	/** Get distinct values for a key */
	public List<Object> distinct (String collectionName, String key, Query query) {
		try {
			Distinct distinct = jongo.getCollection(collectionName).distinct(key);
			if(query != null) {
				distinct.query(query.query, query.params);
			}
			return distinct.as(Object.class);
		} catch (MongoException e) {
			logger.warn("Distint failed", e);
			return null;
		}
	}
	
	/** Aggregate */
	public <T> List<T> aggregate (String collectionName, Query pipeline, Class<T> clazz) {
		return aggregate(collectionName, Arrays.asList(pipeline), clazz);
	}
	/** Aggregate */
	public <T> List<T> aggregate (String collectionName, List<Query> pipelines, Class<T> clazz) {
		try {
			List<T> results = new ArrayList<>();
			Aggregate aggregate = null;
			for(Query pipeline : pipelines) {
				if(aggregate == null) {
					aggregate = jongo.getCollection(collectionName).aggregate(pipeline.query, pipeline.params);
				} else {
					aggregate.and(pipeline.query, pipeline.params);
				}
			}
			if(aggregate != null) {
				Aggregate.ResultsIterator<T> resultsIterator = aggregate.as(clazz);
				while(resultsIterator.hasNext()) {
					results.add(resultsIterator.next());
				}
			}
			return results;
		} catch (MongoException e) {
			logger.warn("Aggregate failed", e);
			return null;
		}
	}

	/** Query container with params */
	public static class Query {
		@Getter
		private String query;
		@Getter
		private Object[] params;
		private Query(String query, Object[] params) {
			this.query = query;
			this.params = params;
		}
		@Override
		public String toString() {
			return JsonUtil.toJson(this);
		}
		@Override
		public boolean equals (Object obj) {
			if(obj != null && obj instanceof Query) {
				Query other = ((Query) obj);
				return Arrays.deepEquals(new Object[] {query, params}, new Object[] {other.query, other.params});
			}
			return false;
		}
		@Override
		public int hashCode () {
			return Arrays.deepHashCode(new Object[] {query, params});
		}
	}
	/** Create a query for document id field */
	public static Query idQuery (Object id) {
		return query("{_id:#}", id);
	}
	/** Create a query */
	public static Query query (String query, Object ... params) {
		return new Query(query, params);
	}
	
	/** Options for FindOne */
	public static class FindOneOptions {
		private Query projection;
		private String orderBy;
		private FindOneOptions() {}
		private FindOneOptions(Query projection, String orderBy) {
			projection(projection);
			orderBy(orderBy);
		}
		public FindOneOptions projection(Query projection) {
			this.projection = projection;
			return this;
		}
		public FindOneOptions orderBy(String orderBy) {
			this.orderBy = orderBy;
			return this;
		}
	}
	/** Create options for FindOne */
	public static FindOneOptions findOneOptions () {
		return new FindOneOptions();
	}
	/** Create options for FindOne */
	public static FindOneOptions findOneOptions (Query projection, String orderBy) {
		return new FindOneOptions(projection, orderBy);
	}
	
	/** Options for Find */
	public static class FindOptions {
		private Query projection;
		private int limit;
		private int skip;
		private String sort;
		private String hint;
		private FindOptions() {}
		private FindOptions(Query projection, int limit, int skip, String sort, String hint) {
			projection(projection);
			limit(limit);
			skip(skip);
			sort(sort);
			hint(hint);
		}
		public FindOptions projection(Query projection) {
			this.projection = projection;
			return this;
		}
		public FindOptions limit(int limit) {
			this.limit = limit;
			return this;
		}
		public FindOptions skip(int skip) {
			this.skip = skip;
			return this;
		}
		public FindOptions sort(String sort) {
			this.sort = sort;
			return this;
		}
		public FindOptions hint(String hint) {
			this.hint = hint;
			return this;
		}
	}
	/** Create options for Find */
	public static FindOptions findOptions () {
		return new FindOptions();
	}
	/** Create options for Find */
	public static FindOptions findOptions (Query projection, int limit, int skip, String sort, String hint) {
		return new FindOptions(projection, limit, skip, sort, hint);
	}
	
	/** Options for FindAndModify */
	public static class FindAndModifyOptions {
		private Query projection;
		private String sort;
		private boolean returnNew;
		private boolean upsert;
		private FindAndModifyOptions() {}
		private FindAndModifyOptions(Query projection, String sort, boolean returnNew, boolean upsert) {
			projection(projection);
			sort(sort);
			returnNew(returnNew);
			upsert(upsert);
		}
		public FindAndModifyOptions projection(Query projection) {
			this.projection = projection;
			return this;
		}
		public FindAndModifyOptions sort(String sort) {
			this.sort = sort;
			return this;
		}
		public FindAndModifyOptions returnNew() {
			return returnNew(true);
		}
		public FindAndModifyOptions returnNew(boolean returnNew) {
			this.returnNew = returnNew;
			return this;
		}
		public FindAndModifyOptions upsert() {
			return upsert(true);
		}
		public FindAndModifyOptions upsert(boolean upsert) {
			this.upsert = upsert;
			return this;
		}
	}
	/** Create options for FindAndModify */
	public static FindAndModifyOptions findAndModifyOptions () {
		return new FindAndModifyOptions();
	}
	/** Create options for FindAndModify */
	public static FindAndModifyOptions findAndModifyOptions (Query projection, String sort, boolean returnNew, boolean upsert) {
		return new FindAndModifyOptions(projection, sort, returnNew, upsert);
	}
	
	/** Options for FindAndRemove */
	public static class FindAndRemoveOptions {
		private Query projection;
		private String sort;
		private FindAndRemoveOptions() {}
		private FindAndRemoveOptions(Query projection, String sort) {
			projection(projection);
			sort(sort);
		}
		public FindAndRemoveOptions projection(Query projection) {
			this.projection = projection;
			return this;
		}
		public FindAndRemoveOptions sort(String sort) {
			this.sort = sort;
			return this;
		}
	}
	/** Create options for FindAndRemove */
	public static FindAndRemoveOptions findAndRemoveOptions () {
		return new FindAndRemoveOptions();
	}
	/** Create options for FindAndRemove */
	public static FindAndRemoveOptions findAndRemoveOptions (Query projection, String sort) {
		return new FindAndRemoveOptions(projection, sort);
	}
	
	/** Options for Update */
	public static class UpdateOptions {
		private boolean upsert;
		private boolean multi;
		private UpdateOptions() {}
		private UpdateOptions(boolean upsert, boolean multi) {
			upsert(upsert);
			multi(multi);
		}
		public UpdateOptions upsert() {
			return upsert(true);
		}
		public UpdateOptions upsert(boolean upsert) {
			this.upsert = upsert;
			return this;
		}
		public UpdateOptions multi() {
			return multi(true);
		}
		public UpdateOptions multi(boolean multi) {
			this.multi = multi;
			return this;
		}
	}
	/** Create options for Update */
	public static UpdateOptions updateOptions () {
		return new UpdateOptions();
	}
	/** Create options for Update */
	public static UpdateOptions updateOptions (boolean upsert, boolean multi) {
		return new UpdateOptions(upsert, multi);
	}

	public static class Config {
		@Getter
		private String dbName = null;
		@Getter
		private Map<String, Integer> servers = null;
		@Getter
		private String username = null;
		@Getter
		private String password = null;
		@Getter
		private String authDB = null;
	}
}