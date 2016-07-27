package integration.logic.data.filter.relations;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.QueryOptions.QueryOptionsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public abstract class Utils {

	private Utils() {
		// prevents instantiation
	}

	protected static CMCard card(final CMCard card) {
		return card;
	}

	protected static String forClass(final CMClass clazz) {
		return clazz.getName();
	}

	protected static QueryOptions query(final QueryOptions... queryOptions) {
		final QueryOptionsBuilder builder = QueryOptions.newQueryOption();
		for (final QueryOptions qo : queryOptions) {
			builder //
			.filter(qo.getFilter()) //
					.orderBy(qo.getSorters());
		}
		return builder.build();
	}

	protected static QueryOptions anyRelation(final CMDomain domain, final CMClass clazz) throws Exception {
		final CMClass destination = (domain.getClass1().equals(clazz)) ? domain.getClass2() : domain.getClass1();
		return QueryOptions.newQueryOption() //
				.filter(json(format("{relation:[{domain: %s, source: %s, destination: %s, direction: _1, type: any}]}", //
						domain.getName(), //
						clazz.getName(), //
						destination.getName()))) //
				.build();
	}

	protected static QueryOptions anyRelated(final CMDomain domain, final CMClass clazz, final CMCard... cards)
			throws Exception {
		final CMClass destination = (domain.getClass1().equals(clazz)) ? domain.getClass2() : domain.getClass1();
		final List<String> jsonCardObjects = Lists.newArrayList();
		for (final CMCard card : cards) {
			jsonCardObjects.add(format("{" + RELATION_CARD_ID_KEY + ": %d, " + RELATION_CARD_CLASSNAME_KEY + ": %s}",
					card.getId(), card.getType().getName()));
		}
		final String jsonCards = join(jsonCardObjects, ",");
		return QueryOptions.newQueryOption()
				//
				.filter(json(format(
						"{relation:[{domain: %s, source: %s, destination: %s, direction: _1, type: oneof, cards: [%s]}]}", //
						domain.getName(), //
						clazz.getName(), //
						destination.getName(), //
						jsonCards))) //
				.build();
	}

	protected static QueryOptions notRelated(final CMDomain domain, final CMClass clazz) throws Exception {
		final CMClass destination = (domain.getClass1().equals(clazz)) ? domain.getClass2() : domain.getClass1();
		return QueryOptions.newQueryOption()
				//
				.filter(json(format(
						"{relation:[{domain: %s, source: %s, destination: %s, type: noone, direction: _1}]}", //
						domain.getName(), //
						clazz.getName(), //
						destination.getName()))) //
				.build();
	}

	protected static QueryOptions sortBy(final String attributeName, final String direction) throws Exception {
		return QueryOptions.newQueryOption() //
				.orderBy(new JSONArray() {
					{
						put(json(format("{property: %s, direction: %s}", attributeName, direction)));
					}
				}).build();
	}

	protected static JSONObject json(final String source) throws Exception {
		return new JSONObject(source);
	}

	protected static CMDomain withDomain(final CMDomain domain) {
		return domain;
	}

	protected static final CMClass withSourceClass(final CMClass clazz) {
		return clazz;
	}

}
