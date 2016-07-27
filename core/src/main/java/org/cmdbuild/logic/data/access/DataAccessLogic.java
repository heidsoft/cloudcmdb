package org.cmdbuild.logic.data.access;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.json.JSONException;

import com.google.common.base.Optional;

/**
 * Business Logic Layer for Data Access
 */
public interface DataAccessLogic extends Logic {

	interface AttributesQuery {

		Integer limit();

		Integer offset();

	}

	CMDataView getView();

	Map<Object, List<RelationInfo>> relationsBySource(String sourceTypeName, DomainWithSource dom);

	GetRelationListResponse getRelationList(Card srcCard, DomainWithSource dom, QueryOptions options);

	GetRelationListResponse getRelationList(Card srcCard, DomainWithSource dom);

	GetRelationListResponse getRelationListEmptyForWrongId(Card srcCard, DomainWithSource dom);

	GetRelationListResponse getRelationList(CMDomain domain, QueryOptions queryOptions);

	Optional<RelationInfo> getRelation(CMDomain domain, Long id);

	Optional<RelationInfo> getRelation(String domain, Long id);

	GetRelationHistoryResponse getRelationHistory(Card srcCard);

	GetRelationHistoryResponse getRelationHistory(Card srcCard, CMDomain domain);

	CMRelation getRelation(Long srcCardId, Long dstCardId, CMDomain domain, CMClass sourceClass,
			CMClass destinationClass);

	Optional<RelationInfo> getHistoricRelation(String domain, Long id);

	Iterable<Card> getCardHistory(Card srcCard, boolean allAttributes);

	Card fetchHistoricCard(String className, Long cardId);

	CMClass findClass(Long classId);

	CMClass findClass(String className);

	CMDomain findDomain(Long domainId);

	CMDomain findDomain(String domainName);

	boolean hasClass(Long classId);

	/**
	 * 
	 * @return only active classes (all classes, included superclasses, simple
	 *         classes and process classes).
	 */
	Iterable<? extends CMClass> findActiveClasses();

	/**
	 * 
	 * @return active and non active domains
	 */
	Iterable<? extends CMDomain> findAllDomains();

	/**
	 * 
	 * @return only active domains
	 */
	Iterable<? extends CMDomain> findActiveDomains();

	Iterable<? extends CMDomain> findReferenceableDomains(String className);

	/**
	 * 
	 * @return active and non active classes
	 */
	Iterable<? extends CMClass> findAllClasses();

	/**
	 * 
	 * @return all {@link CMClass} according with specified status.
	 */
	Iterable<? extends CMClass> findClasses(boolean activeOnly);

	PagedElements<CMAttribute> getAttributes(String className, boolean onlyActive, AttributesQuery attributesQuery);

	PagedElements<CMAttribute> getDomainAttributes(String className, boolean onlyActive, AttributesQuery attributesQuery);

	/**
	 * Fetches the card with the specified Id from the class with the specified
	 * name
	 * 
	 * @param className
	 * @param cardId
	 * @throws NoSuchElementException
	 *             if the card with the specified Id number does not exist or it
	 *             is not unique
	 * @return the card with the specified Id.
	 */
	Card fetchCard(String className, Long cardId);

	CMCard fetchCMCard(String className, Long cardId);

	Card fetchCardShort(String className, Long cardId, QueryOptions queryOptions);

	Card fetchCard(Long classId, Long cardId);

	/**
	 * Retrieve the cards of a given class that matches the given query options
	 * 
	 * @param className
	 * @param queryOptions
	 * @return a FetchCardListResponse
	 */
	PagedElements<Card> fetchCards(String className, QueryOptions queryOptions);

	/**
	 * Execute a given SQL function to select a set of rows Return these rows as
	 * fake cards
	 * 
	 * @param functionName
	 * @param queryOptions
	 * @return
	 */
	PagedElements<Card> fetchSQLCards(String functionName, QueryOptions queryOptions);

	/**
	 * 
	 * @param className
	 * @param cardId
	 * @param queryOptions
	 * @return a long (zero based) with the position of this card in relation of
	 *         current sorting and filter
	 */
	CMCardWithPosition getCardPosition(String className, Long cardId, QueryOptions queryOptions);

	PagedElements<CMCardWithPosition> fetchCardsWithPosition(String className, QueryOptions queryOptions, Long cardId);

	/**
	 * Call createCard forwarding the given card, and saying to manage also the
	 * attributes over references domains
	 * 
	 * @param card
	 * @return
	 */
	Long createCard(Card card);

	/**
	 * 
	 * @param userGivenCard
	 * @param manageAlsoDomainsAttributes
	 *            if true iterate over the attributes to extract the ones with
	 *            type ReferenceAttributeType. For that attributes fetch the
	 *            relation and update the attributes if present in the
	 *            userGivenCard
	 * @return
	 */
	Long createCard(Card userGivenCard, boolean manageAlsoDomainsAttributes);

	void updateCard(Card card);

	void updateCards(Iterable<Card> cards);

	void updateFetchedCard(Card card, Map<String, Object> attributes);

	void deleteCard(String className, Long cardId);

	/**
	 * Retrieves all domains in which the class with id = classId is involved
	 * (both direct and inverse relation)
	 * 
	 * @param className
	 *            the class name involved in the relation
	 * @param skipDisabledClasses
	 * 
	 * @return a list of all domains defined for the class
	 */
	Iterable<CMDomain> findDomainsForClass(String className, boolean skipDisabledClasses);

	/**
	 * Tells if the given class is a subclass of Activity
	 * 
	 * @return {@code true} if if the given class is a subclass of Activity,
	 *         {@code false} otherwise
	 */
	boolean isProcess(CMClass target);

	/**
	 * Relations.... move the following code to another class
	 * 
	 * @return all created relation'ids.
	 */
	Iterable<Long> createRelations(RelationDTO relationDTO);

	void updateRelation(RelationDTO relationDTO);

	void deleteRelation(String domainName, Long relationId);

	void deleteDetail(Card master, Card detail, String domainName);

	public void deleteRelation(String srcClassName, Long srcCardId, String dstClassName, Long dstCardId, CMDomain domain);

	File exportClassAsCsvFile(String className, String separator);

	CSVData importCsvFileFor(DataHandler csvFile, Long classId, String separator) throws IOException, JSONException;

	CMCard resolveCardReferences(CMClass entryType, CMCard card);

}
