/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.pointset.rsc;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.colormap.ColorMapException;
import com.raytheon.uf.common.colormap.ColorMapLoader;
import com.raytheon.uf.common.colormap.image.ColorMapData;
import com.raytheon.uf.common.colormap.image.ColorMapData.ColorMapDataType;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.pointset.PointSetRecord;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.numeric.UnsignedNumbers;
import com.raytheon.uf.common.style.AbstractStylePreferences;
import com.raytheon.uf.common.style.ParamLevelMatchCriteria;
import com.raytheon.uf.common.style.StyleException;
import com.raytheon.uf.common.style.StyleManager;
import com.raytheon.uf.common.style.StyleManager.StyleType;
import com.raytheon.uf.common.style.StyleRule;
import com.raytheon.uf.common.style.image.ColorMapParameterFactory;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.util.BufferUtil;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.PaintStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged.ChangeType;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.pointset.image.PointSetDataCallback;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Resource for rendering {@link PointSetRecord} data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------
 * Aug 28, 2015  4709     bsteffen  Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PointSetResource extends
        AbstractVizResource<PointSetResourceData, IMapDescriptor> {

    private final PointSetStageJob stageJob = new PointSetStageJob();

    private StyleRule styleRule;

    private Map<DataTime, PointSetFrame> frames = new HashMap<>();

    protected PointSetResource(PointSetResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties);
        this.dataTimes = new ArrayList<>();
    }

    @Override
    protected void resourceDataChanged(ChangeType type, Object updateObject) {
        if (type == ChangeType.DATA_UPDATE) {
            for (PluginDataObject pdo : (PluginDataObject[]) updateObject) {
                addRecord((PointSetRecord) pdo);
            }
        }
        super.resourceDataChanged(type, updateObject);
    }

    public ColorMapParameters createColorMapParameters(PointSetRecord record)
            throws VizException {
        ParamLevelMatchCriteria matchCriteria = new ParamLevelMatchCriteria();
        matchCriteria.setParameterName(Arrays.asList(record.getParameter()
                .getAbbreviation()));
        // TODO convert level to a style level.
        //matchCriteria.setLevels(new ArrayList<Level>());
        matchCriteria.setCreatingEntityNames(Arrays.asList(record
                .getDatasetId()));
        try {
            styleRule = StyleManager.getInstance().getStyleRule(
                    StyleType.IMAGERY, matchCriteria);
            if (styleRule != null) {
                return ColorMapParameterFactory.build(styleRule, record
                        .getParameter().getUnit());
            } else {
                ColorMapData data = new PointSetDataCallback(record)
                        .getColorMapData();
                ColorMapDataType type = data.getDataType();
                Buffer buffer = BufferUtil.duplicate(data.getBuffer());
                buffer.rewind();
                float min = Float.POSITIVE_INFINITY;
                float max = Float.NEGATIVE_INFINITY;
                while (buffer.hasRemaining()) {
                    float val = extractVal(type, buffer);
                    if (val < min) {
                        min = val;
                    }
                    if (val > max) {
                        max = val;
                    }
                }

                return ColorMapParameterFactory.build(min, max,
                        data.getDataUnit(), matchCriteria);
            }
        } catch (StyleException e) {
            throw new VizException(e);
        }
    }

    private float extractVal(ColorMapDataType type, Buffer buffer)
            throws VizException {
        switch (type) {
        case SIGNED_BYTE:
            return ((ByteBuffer) buffer).get();
        case BYTE:
            return UnsignedNumbers.ubyteToShort(((ByteBuffer) buffer).get());
        case SHORT:
            return ((ShortBuffer) buffer).get();
        case UNSIGNED_SHORT:
            return UnsignedNumbers.ushortToInt(((ShortBuffer) buffer).get());
        case INT:
            return ((IntBuffer) buffer).get();
        case FLOAT:
            return ((FloatBuffer) buffer).get();
        case DOUBLE:
            return (float) ((DoubleBuffer) buffer).get();
        default:
            throw new VizException("Unable to handle data of type: " + type);

        }
    }

    public void addRecord(PointSetRecord record) {
        synchronized (frames) {
            if (frames.isEmpty() && getStatus() == ResourceStatus.INITIALIZED) {
                try {
                    /*
                     * Normally this happens in init but this is a fallback if
                     * the resource was inited with no data.
                     */
                    createColorMapParameters(record);
                } catch (VizException e) {
                    statusHandler.error(
                            "Unable to create color map parameters.", e);
                }
            }
            frames.put(record.getDataTime(), new PointSetFrame(record, this));
            dataTimes.add(record.getDataTime());
            Collections.sort(dataTimes);

        }
    }

    @Override
    public void remove(DataTime dataTime) {
        synchronized (frames) {
            PointSetFrame frame = frames.remove(dataTime);
            frame.dispose();
            super.remove(dataTime);
        }
    }

    @Override
    protected void disposeInternal() {
        synchronized (frames) {
            for (PointSetFrame frame : frames.values()) {
                frame.dispose();
            }
            frames.clear();
        }
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        PointSetFrame frame = frames.get(paintProps.getDataTime());
        if (frame == null) {
            return;
        }
        boolean painted = frame.paint(paintProps, target);
        if (!painted) {
            stageJob.schedule(frame);
            updatePaintStatus(PaintStatus.INCOMPLETE);
        }
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        if (!frames.isEmpty()) {
            ColorMapParameters colorMapParameters = createColorMapParameters(frames
                    .values().iterator().next().getRecord());
            if (colorMapParameters.getColorMapName() == null) {
                colorMapParameters.setColorMapName("Grid/gridded data");
            }
            try {
                colorMapParameters.setColorMap(ColorMapLoader
                        .loadColorMap(colorMapParameters.getColorMapName()));
            } catch (ColorMapException e) {
                throw new VizException(e);
            }
            getCapability(ColorMapCapability.class).setColorMapParameters(
                    colorMapParameters);
        }
    }

    @Override
    public String getName() {
        PointSetFrame frame = frames.get(descriptor.getTimeForResource(this));
        if (frame == null) {
            return "Point Set Data";
        } else {
            PointSetRecord record = frame.getRecord();
            String unitStr = record.getParameter().getUnitString();
            boolean includeLevel = true;
            if (styleRule != null) {
                AbstractStylePreferences prefs = styleRule.getPreferences();
                includeLevel = !prefs.getDisplayFlags()
                        .hasFlag("NoPlane");
                String prefsUnitStr = prefs.getDisplayUnitLabel();
                if (prefsUnitStr != null) {
                    unitStr = prefs.getDisplayUnitLabel();
                }
            } else {
                ColorMapParameters parameters = getCapability(
                        ColorMapCapability.class).getColorMapParameters();
                if (parameters != null) {
                    Unit<?> unit = parameters.getDisplayUnit();
                    if (unit != null) {
                        unitStr = UnitFormat.getUCUMInstance().format(unit);
                    }
                }
            }
            String datasetIdPart = frame.getRecord().getDatasetId();
            String levelPart = "";
            if (includeLevel) {
                levelPart = " " + record.getLevel();
            }
            String paramPart = " " + record.getParameter().getName() + " ("
                    + unitStr
                    + ")";
            return datasetIdPart + levelPart + paramPart;
        }
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        PointSetFrame frame = frames.get(descriptor.getTimeForResource(this));
        if (frame == null) {
            return null;
        } else {
            try {
                Coordinate ll = coord.asLatLon();
                double[] pix = descriptor.worldToPixel(new double[] { ll.x,
                        ll.y });
                return frame.inspect(pix[0], pix[1]);
            } catch (FactoryException | TransformException e) {
                throw new VizException(e);
            }
        }
    }

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        synchronized (frames) {
            for (PointSetFrame frame : frames.values()) {
                frame.dispose();
            }
        }
    }

}
