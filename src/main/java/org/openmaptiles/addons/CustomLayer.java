package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import org.openmaptiles.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLayer implements Layer, org.openmaptiles.OpenMapTilesProfile.CustomLayersProcessor {
  protected String LAYER_NAME;
  private static final Logger LOGGER = LoggerFactory.getLogger(CustomLayer.class);

  public CustomLayer() {
  }

  @Override
  public void release() {
  }

  @Override
  public String name() {
    LOGGER.info("name(): " + LAYER_NAME);
    return LAYER_NAME;
  }

  @Override
  public void processCustomLayers(SourceFeature feature, FeatureCollector features) {
    LOGGER.info("feature.getSourceLayer(): " + feature.getSourceLayer());
    LOGGER.info("LAYER_NAME: " + LAYER_NAME);
    if (feature.getSourceLayer().equals(LAYER_NAME)) {
      LOGGER.info("including feature: " + feature);
      features.polygon(LAYER_NAME)
        .putAttrs(feature.tags());
    }
  }
}

