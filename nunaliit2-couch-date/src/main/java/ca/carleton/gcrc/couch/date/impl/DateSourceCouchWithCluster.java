package ca.carleton.gcrc.couch.date.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.couch.client.CouchDesignDocument;
import ca.carleton.gcrc.couch.client.CouchQuery;
import ca.carleton.gcrc.couch.client.CouchQueryResults;
import ca.carleton.gcrc.couch.date.cluster.Tree;
import ca.carleton.gcrc.couch.date.cluster.CouchIntervalClusterTreeFactory;

public class DateSourceCouchWithCluster implements DateSource, SerializableToDot, SerializableToInfo {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private CouchDesignDocument atlasDesignDocument;	
	private Tree clusterTree = null;
	
	public DateSourceCouchWithCluster(CouchDesignDocument atlasDesignDocument) throws Exception {
		this.atlasDesignDocument = atlasDesignDocument;

		try {
			CouchIntervalClusterTreeFactory factory = new CouchIntervalClusterTreeFactory();
			clusterTree = factory.createClusterTree(atlasDesignDocument);
		} catch(Exception e) {
			logger.error("Unable to create a date cluster tree",e);
			throw new Exception("Unable to create a date cluster tree",e);
		}
	}
	
	public Tree getClusterTree(){
		return clusterTree;
	}
	
	@Override
	public SearchResults getAllDateIntervals() throws Exception {
		SearchResults results = new SearchResults();
		
		CouchQuery query = new CouchQuery();
		query.setViewName("date-index");
		query.setIncludeDocs(false);
		query.setReduce(false);
		CouchQueryResults queryResults = atlasDesignDocument.performQuery(query);
		
		results.documentWithIntervals = new ArrayList<DocumentWithInterval>(queryResults.getRows().size());
		for(JSONObject row : queryResults.getRows()){
			String docId = row.optString("id");
			JSONArray jsonInterval = row.optJSONArray("value");
			long min = 0;
			long max = -1;
			if( null != jsonInterval && jsonInterval.length() >= 2 ){
				min = jsonInterval.optLong(0);
				max = jsonInterval.optLong(1);
			}
			if( null != docId && max >= min ){
				Interval interval = new Interval(min,max);
				DocumentWithInterval docWithInt = new DocumentWithInterval(docId, interval);
				results.documentWithIntervals.add(docWithInt);
			}
		}
		
		return results;
	}

	@Override
	public SearchResults getDateIntervalsIntersectingWith(Interval interval) throws Exception {
		SearchResults results = new SearchResults();
		
		List<Integer> clusterIds = clusterTree.clusterIdsFromInterval(interval);
		results.clusterCount = clusterIds.size();
		JSONArray keys = new JSONArray();
		keys.put(JSONObject.NULL); // always include un-indexed intervals
		for(Integer clusterId : clusterIds){
			keys.put(clusterId.intValue());
		}
		
		CouchQuery query = new CouchQuery();
		query.setViewName("date-index");
		query.setIncludeDocs(false);
		query.setReduce(false);
		query.setKeys(keys);
		CouchQueryResults queryResults = atlasDesignDocument.performQuery(query);
		
		results.documentWithIntervals = new ArrayList<DocumentWithInterval>(queryResults.getRows().size());
		for(JSONObject row : queryResults.getRows()){
			String docId = row.optString("id");
			JSONArray key = row.optJSONArray("value");
			results.intervalCount++;
			long min = 0;
			long max = -1;
			if( null != key && key.length() >= 2 ){
				min = key.optLong(0);
				max = key.optLong(1);
			}
			if( null != docId && max >= min ){
				Interval docInterval = new Interval(min,max);
				if( docInterval.intersectsWith(interval) ){
					DocumentWithInterval docWithInt = new DocumentWithInterval(docId, docInterval);
					results.documentWithIntervals.add(docWithInt);
					results.intervalMatched++;
				}
			}
		}
		
		return results;
	}

	@Override
	public void printDot(PrintWriter pw) throws Exception {
		Tree.treeToDot(clusterTree, pw);
	}

	@Override
	public void printInfo(PrintWriter pw) throws Exception {
		Tree.treeToInfo(clusterTree, pw);
	}
}
