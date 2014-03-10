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
package com.raytheon.uf.common.dataaccess.response;

import javax.measure.unit.UnitFormat;

import com.raytheon.uf.common.dataaccess.grid.IGridData;
import com.raytheon.uf.common.geospatial.interpolation.data.FloatArrayWrapper;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * IGridData wrapper used as part of <code>GetGridDataResponse</code>.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 04, 2013           dgilling    Initial creation
 * Feb 24, 2014  2762     bsteffen    Format units with UCUM
 * 
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

@DynamicSerialize
public class GridResponseData extends AbstractResponseData {

    @DynamicSerializeElement
    private String parameter;

    @DynamicSerializeElement
    private String unit;

    @DynamicSerializeElement
    private float[] gridData;

    public GridResponseData() {
        // no-op, for serialization
    }

    public GridResponseData(final IGridData data) {
        super(data);

        parameter = data.getParameter();
        if (data.getUnit() != null) {
            unit = UnitFormat.getUCUMInstance().format(data.getUnit());
        }
        FloatArrayWrapper dataGrid = new FloatArrayWrapper(
                data.getGridGeometry());
        dataGrid = data.populateData(dataGrid);
        gridData = dataGrid.getArray();
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float[] getGridData() {
        return gridData;
    }

    public void setGridData(float[] gridData) {
        this.gridData = gridData;
    }
}
