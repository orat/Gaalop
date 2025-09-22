package de.gaalop.visualizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Defines an interface for something to render
 * @author Christian Steinmetz
 */
public interface Rendering {
    
    /**
     * Determines, if a new data set is available
     * @return <value>true</value> if a new data set is available, otherwise <value>false</value>
     */
    public boolean isNewDataSetAvailable();
    
    /**
     * Returns the point clouds to render
     * @return The point clouds
     */
    public Map<String, PointCloud> getDataSet();
    
    /**
     * Returns a set of all visible objects
     * @return The set of all visible objects
     */
    public Set<String> getVisibleObjects();
    
    /**
     * Returns the loaded pointClouds
     * @return The loaded pointClouds instance
     */
    public PointClouds getLoadedPointClouds();
    
}
