package org.cmdbuild.cmdbf.cmdbmdr;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.cmdbuild.cmdbf.CMDBfId;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.cql.compiler.CQLCompiler;
import org.cmdbuild.cql.compiler.CQLCompilerListener;
import org.cmdbuild.cql.compiler.impl.FactoryImpl;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.cql.facade.CQLAnalyzer;
import org.cmdbuild.cql.facade.CQLAnalyzer.NullCallback;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;

public class MdrScopedIdRegistry {
	static private final Pattern localIdPattern = Pattern.compile("^(\\w+)-(\\d+)(?:#(.*))?$");
	static private final String MDRID_PREFIX = "CMDBuild:";
	static private final int INSTANCETYPE_GROUP = 1;
	static private final int INSTANCEID_GROUP = 2;
	static private final int RECORDID_GROUP = 3;
	private static final Alias TARGET_ALIAS = NameAlias.as("TARGET");
	private static final Alias DOMAIN_ALIAS = NameAlias.as("DOMAIN");

	private final CMDataView view;
	private final CmdbfConfiguration cmdbfConfiguration;
	private final List<ReconciliationRule> rules;

	private class CQLCallback extends NullCallback {

		private final QuerySpecsBuilder queryBuilder;
		private CMClass type;

		public CQLCallback() {
			queryBuilder = view.select();
			type = null;
		}

		public QuerySpecsBuilder getQueryBuilder() {
			return queryBuilder;
		}

		public CMClass getType() {
			return type;
		}

		@Override
		public void from(final CMClass source) {
			type = source;
			queryBuilder.select(anyAttribute(source)).from(source);
		}

		@Override
		public void distinct() {
			queryBuilder.distinct();
		}

		@Override
		public void leftJoin(final CMClass target, final Alias alias, final Over over) {
			queryBuilder.leftJoin(target, alias, over);
		}

		@Override
		public void join(final CMClass target, final Alias alias, final Over over) {
			queryBuilder.join(target, alias, over);
		}

		@Override
		public void where(final WhereClause clause) {
			queryBuilder.where(clause);
		}
	}

	public MdrScopedIdRegistry(final DataAccessLogic dataAccessLogic, final CmdbfConfiguration cmdbfConfiguration)
			throws Exception {
		this.view = dataAccessLogic.getView();
		this.cmdbfConfiguration = cmdbfConfiguration;

		if (cmdbfConfiguration.getReconciliationRules() != null) {
			rules = loadRules(cmdbfConfiguration.getReconciliationRules());
		} else {
			rules = Collections.emptyList();
		}
	}

	public boolean isLocal(final MdrScopedIdType id) {
		return cmdbfConfiguration.getMdrId().equals(id.getMdrId());
	}

	public CMCard resolveItemAlias(final MdrScopedIdType id) throws Exception {
		CMCard card = null;
		if (!isLocal(id)) {
			final ReconciliationRule rule = findRule(id);
			if (rule != null && rule.getQuery() != null) {
				final Map<String, Object> context = getParams(rule.getRegex(), id.getLocalId());
				card = findCard(rule.getType(), rule.getQuery(), context);
			}
		}
		return card;
	}

	public CmdbRelation resolveRelationshipAlias(final MdrScopedIdType id) throws Exception {
		CmdbRelation relation = null;
		if (!isLocal(id)) {
			final ReconciliationRule rule = findRule(id);
			if (rule != null && rule.getSource() != null && rule.getTarget() != null && rule.getType() != null) {
				final Map<String, Object> context = getParams(rule.getRegex(), id.getLocalId());
				final CMCard source = findCard(rule.getSourceType(), rule.getSource(), context);
				final CMCard target = findCard(rule.getTargetType(), rule.getTarget(), context);
				final CMDomain type = view.findDomain(format(rule.getType(), null, null, context.entrySet()));
				if (source != null && target != null && type != null) {
					final QuerySpecsBuilder queryBuilder = view.select(anyAttribute(DOMAIN_ALIAS));
					queryBuilder.from(source.getType());
					queryBuilder.join(target.getType(), TARGET_ALIAS, over(type, as(DOMAIN_ALIAS)));
					queryBuilder.where(and(
							condition(attribute(DOMAIN_ALIAS, SystemAttributes.DomainId1.getDBName()),
									eq(source.getId())),
							condition(attribute(DOMAIN_ALIAS, SystemAttributes.DomainId2.getDBName()),
									eq(target.getId()))));

					for (final CMQueryRow row : queryBuilder.run()) {
						if (relation == null) {
							final CMRelation cmRelation = row.getRelation(DOMAIN_ALIAS).getRelation();
							relation = new CmdbRelation(cmRelation, source.getType().getName(), target.getType()
									.getName());
						} else {
							throw new IllegalArgumentException("Id resolved to more than one relationship");
						}
					}
				}
			}
		}
		return relation;
	}

	public Set<CMDBfId> getItemAlias(final Long id, final String type, final Iterable<Map.Entry<String, Object>> values) {
		final Set<CMDBfId> aliasList = new HashSet<CMDBfId>();
		final CMClass cmType = view.findClass(type);
		for (final ReconciliationRule rule : rules) {
			if (rule.getFilter() != null && rule.getFormat() != null) {
				boolean match = type.equals(rule.getFilter());
				if (!match) {
					final CMClass ruleType = view.findClass(rule.getFilter());
					if (ruleType != null) {
						match = ruleType.isAncestorOf(cmType);
					}
				}
				if (match) {
					final String localId = format(rule.getFormat(), id, type, values);
					final CMDBfId cmdbfId = new CMDBfId(rule.getMdrId(), localId);
					aliasList.add(cmdbfId);
				}
			}
		}
		return aliasList;
	}

	public Set<CMDBfId> getRelationshipAlias(final Long id, final String type,
			final Iterable<Map.Entry<String, Object>> values) {
		final Set<CMDBfId> aliasList = new HashSet<CMDBfId>();
		for (final ReconciliationRule rule : rules) {
			if (rule.getFilter() != null && rule.getFormat() != null) {
				boolean match = type.equals(rule.getFilter());
				if (!match) {
					match = "Domain".equals(rule.getFilter());
				}
				if (match) {
					final String localId = format(rule.getFormat(), id, type, values);
					final CMDBfId cmdbfId = new CMDBfId(rule.getMdrId(), localId);
					aliasList.add(cmdbfId);
				}
			}
		}
		return aliasList;
	}

	private ReconciliationRule findRule(final MdrScopedIdType id) throws Exception {
		ReconciliationRule rule = null;
		if (id.getMdrId().startsWith(MDRID_PREFIX)) {
			rule = ReconciliationRule.parse(id.getMdrId().substring(MDRID_PREFIX.length()));
		}
		final Iterator<ReconciliationRule> iterator = rules.iterator();
		while (rule == null && iterator.hasNext()) {
			final ReconciliationRule currentRule = iterator.next();
			if (currentRule.getMdrId().equals(id.getMdrId())
					&& currentRule.getRegex().matcher(id.getLocalId()).matches()) {
				rule = currentRule;
			}
		}
		return rule;
	}

	private String format(final String format, final Long id, final String type,
			final Iterable<Map.Entry<String, Object>> values) {
		final Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
		final Matcher matcher = pattern.matcher(format);
		final StringBuilder builder = new StringBuilder();
		int i = 0;
		while (matcher.find()) {
			String replacement = null;
			if (matcher.group(1).equals("Type")) {
				replacement = type;
			} else if (matcher.group(1).equals("Id")) {
				replacement = id.toString();
			} else {
				final Iterator<Entry<String, Object>> iterator = values.iterator();
				while (replacement == null && iterator.hasNext()) {
					final Entry<String, Object> entry = iterator.next();
					if (entry.getKey().equals(matcher.group(1))) {
						replacement = entry.getValue() != null ? entry.getValue().toString() : "";
					}
				}
			}
			builder.append(format.substring(i, matcher.start()));
			if (replacement == null) {
				builder.append(matcher.group(0));
			} else {
				builder.append(replacement);
			}
			i = matcher.end();
		}
		builder.append(format.substring(i, format.length()));
		return builder.toString();
	}

	private CMCard findCard(final String type, final String query, final Map<String, Object> context) throws Exception {
		CMCard card = null;
		final CQLCallback callback = new CQLCallback();

		final CQLCompiler cqlCompiler = new CQLCompiler();
		final CQLCompilerListener cqlListener = new CQLCompilerListener();
		cqlListener.setFactory(new FactoryImpl());
		FactoryImpl.CmdbuildCheck = true;
		cqlCompiler.compile(query, cqlListener);
		final QueryImpl cqlQuery = (QueryImpl) cqlListener.getRootQuery();

		if (type != null) {
			cqlQuery.getFrom().mainClass().setName(format(type, null, null, context.entrySet()));
		}
		cqlQuery.check();

		CQLAnalyzer.analyze(cqlQuery, context, callback);
		for (final CMQueryRow row : callback.getQueryBuilder().run()) {
			if (card == null) {
				card = row.getCard(callback.getType());
			} else {
				throw new IllegalArgumentException("Id resolved to more than one item");
			}
		}
		return card;
	}

	private Map<String, Object> getParams(final Pattern regex, final String text) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final Matcher matcher = regex.matcher(text);
		if (matcher.matches()) {
			for (int i = 0; i <= matcher.groupCount(); i++) {
				params.put("g" + Integer.toString(i), matcher.group(i));
			}
		}
		return params;
	}

	private List<ReconciliationRule> loadRules(final String path) throws Exception {
		final JAXBContext jaxbContext = JAXBContext.newInstance(ReconciliationRule.class);

		final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		final StreamSource xml = new StreamSource(path);
		final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(xml);

		final List<ReconciliationRule> rules = new ArrayList<ReconciliationRule>();
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		while (reader.getEventType() != XMLStreamConstants.END_DOCUMENT) {
			if (reader.isStartElement() && "Rule".equals(reader.getLocalName())) {
				final ReconciliationRule rule = (ReconciliationRule) unmarshaller.unmarshal(reader);
				rules.add(rule);
			}
			reader.next();
		}
		return rules;
	}

	public CMDBfId getCMDBfId(final Long instanceId, final String type, final String recordId) {
		final StringBuffer localId = new StringBuffer();
		localId.append(type);
		localId.append('-');
		localId.append(Long.toString(instanceId));
		if (recordId != null) {
			localId.append('#');
			localId.append(recordId);
		}
		return new CMDBfId(cmdbfConfiguration.getMdrId(), localId.toString());
	}

	public CMDBfId getCMDBfId(final Long id, final String type) {
		return getCMDBfId(id, type, null);
	}

	public CMDBfId getCMDBfId(final CMEntry element) {
		return getCMDBfId(element, null);
	}

	public CMDBfId getCMDBfId(final CMEntry element, final String recordId) {
		return getCMDBfId(element.getId(), element.getType().getName(), recordId);
	}

	public CMDBfId getCMDBfId(final MdrScopedIdType id, final String recordId) {
		return getCMDBfId(getInstanceId(id), getInstanceType(id), recordId);
	}

	public Long getInstanceId(final MdrScopedIdType id) {
		Long instanceId = null;
		final Matcher matcher = localIdPattern.matcher(id.getLocalId());
		if (matcher.matches()) {
			instanceId = Long.parseLong(matcher.group(INSTANCEID_GROUP));
		}
		return instanceId;
	}

	public String getInstanceType(final MdrScopedIdType id) {
		String instanceType = null;
		final Matcher matcher = localIdPattern.matcher(id.getLocalId());
		if (matcher.matches()) {
			instanceType = matcher.group(INSTANCETYPE_GROUP);
		}
		return instanceType;
	}

	public String getRecordId(final MdrScopedIdType id) {
		String recordId = null;
		final Matcher matcher = localIdPattern.matcher(id.getLocalId());
		if (matcher.matches()) {
			recordId = matcher.group(RECORDID_GROUP);
		}
		return recordId;
	}
}
