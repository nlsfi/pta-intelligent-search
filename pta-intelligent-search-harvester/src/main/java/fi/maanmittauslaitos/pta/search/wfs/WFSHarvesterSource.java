package fi.maanmittauslaitos.pta.search.wfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;

import fi.maanmittauslaitos.pta.search.HarvesterSource;
import fi.maanmittauslaitos.pta.search.HarvestingException;

public class WFSHarvesterSource extends HarvesterSource {
	private static Logger logger = Logger.getLogger(WFSHarvesterSource.class);
	
	private String version;
	private String featureType;
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	
	public String getFeatureType() {
		return featureType;
	}
	
	
	@Override
	public Iterator<InputStream> iterator() {
		try {
			String getCapabilities = getOnlineResource()+"?SERVICE=WFS&REQUEST=GetCapabilities&VERSION="+getVersion();
	
			Map<String, Object> connectionParameters = new HashMap<>();
			connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
			connectionParameters.put("WFSDataStoreFactory:BUFFER_SIZE", getBatchSize());
			connectionParameters.put("WFSDataStoreFactory:WFS_STRATEGY", "nonstrict");
	
			DataStore data = DataStoreFinder.getDataStore( connectionParameters );
	
			SimpleFeatureSource source = data.getFeatureSource(getFeatureType());
			
			return new FeatureToInputStreamIterator(source.getFeatures());
			
		} catch(IOException e) {
			throw new HarvestingException(e);
		}
		//return new WFSIterator();
	}
	
	private class FeatureToInputStreamIterator implements Iterator<InputStream> {
		private SimpleFeatureIterator iterator;
		
		public FeatureToInputStreamIterator(SimpleFeatureCollection collection) {
			iterator = collection.features();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public InputStream next() {
			
			SimpleFeature feature = iterator.next();
			
			FeatureJSON io = new FeatureJSON();
			
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				io.writeFeature(feature, baos);
				
				return new ByteArrayInputStream(baos.toByteArray());
			} catch(IOException e) {
				throw new HarvestingException(e);
			}
		}
	
	}
	
	private class WFSIterator implements Iterator<InputStream> {
		private final SimpleFeatureSource source;
		
		private SimpleFeatureIterator currentIterator;
		
		private int numberOfRecordsProcessed = 0;
		private int numberOfRecordsInService;
		private boolean failed = false;
		
		public WFSIterator() {
			try {
				String getCapabilities = getOnlineResource()+"?SERVICE=WFS&REQUEST=GetCapabilities&VERSION="+getVersion();

				Map<String, Object> connectionParameters = new HashMap<>();
				connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
				connectionParameters.put("WFSDataStoreFactory:MAXFEATURES", getBatchSize());

				DataStore data = DataStoreFinder.getDataStore( connectionParameters );

				source = data.getFeatureSource(getFeatureType());
				
				//getNextBatch();
			} catch(IOException e) {
				throw new HarvestingException(e);
			}

		}
		
		/*
		private void getNextBatch() throws IOException {
			logger.debug("Requesting records starting at position "+(1+numberOfRecordsProcessed)+", batch size is "+getBatchSize());
			
			
			// Retrieve first batch
			Query foo = new Query();
			foo.setStartIndex(numberOfRecordsProcessed);
			foo.setMaxFeatures(getBatchSize());
			
			SimpleFeatureCollection coll = source.getFeatures(foo);
			
			coll.
			
			currentIterator = coll.features();
		}
		*/
		@Override
		public boolean hasNext() {
			if (failed) return false;
			
			return numberOfRecordsProcessed < numberOfRecordsInService;
		}
		
		@Override
		public InputStream next() {
			if (failed) return null;
			
			
			// TODO Auto-generated method stub
			return null;
		}
	}

}
