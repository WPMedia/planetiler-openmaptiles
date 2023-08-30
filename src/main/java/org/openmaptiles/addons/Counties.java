package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import org.openmaptiles.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Counties implements Layer, org.openmaptiles.OpenMapTilesProfile.CustomLayersProcessor {
  private static final String LAYER_NAME = "counties";
  private static final Logger LOGGER = LoggerFactory.getLogger(Counties.class);

  public Counties() {
  }

  @Override
  public void release() {
  }

  @Override
  public String name() {
    return LAYER_NAME;
  }

  @Override
  public void processCustomLayers(SourceFeature feature, FeatureCollector features) {
    if (feature.getSourceLayer().equals(LAYER_NAME)) {
      features.polygon(LAYER_NAME)
        .putAttrs(feature.tags());
    }
  }
}

