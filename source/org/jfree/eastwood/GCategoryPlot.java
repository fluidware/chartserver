/* ===============
 * Eastwood Charts
 * ===============
 *
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Project Info:  http://www.jfree.org/eastwood/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------------
 * GCategoryPlot.java
 * ------------------
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Original Author:  Niklas Therning;
 *
 * Changes
 * -------
 * 30-Jun-2008 : Version 1 (NT);
 *
 */

package org.jfree.eastwood;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;

/**
 * A custom plot class (adds support for specifying the step size to use for
 * grid lines). NOTE: X axis step size has not been fully implemented. We will
 * always place the vertical grid lines in the middle of each bar if
 * <code>xAxisStepSize &gt; 0</code>. If <code>xAxisStepSize == 0</code>
 * vertical grid lines will be disabled.
 */
class GCategoryPlot extends CategoryPlot {

    /** The step size to use when drawing the grid lines for the x axis. */
    private double xAxisStepSize;

    /** The step size to use when drawing the grid lines for the y axis. */
    private double yAxisStepSize;


    /**
    * A custom label generator.
    */
    static class LabelGenerator extends StandardCategoryItemLabelGenerator {
        /**
         * Generates an item label.
         *
         * @param dataset  the dataset.
         * @param series  the series index.
         * @param category  the category index.
         *
         * @return the label.
         */
        private HashMap predefinedLabels = new HashMap<String, String>();
        public void setLabel(int series, int category, String label){
            predefinedLabels.put(Integer.toString(series) + "_" + Integer.toString(category), label);
        }
        @Override
        public String generateLabel(CategoryDataset dataset, int series, int category) {
            if (predefinedLabels.containsKey(Integer.toString(series) + "_" + Integer.toString(category))){
                return (String)predefinedLabels.get(Integer.toString(series) + "_" + Integer.toString(category));
            } else {
                return "";
            }
        }
    }
    /**
     * Default constructor.
     */
    public GCategoryPlot() {
        this.xAxisStepSize = 0.0;
        this.yAxisStepSize = 0.0;
    }

    /**
     * Sets the step size to use when drawing the grid lines for the x axis.
     *
     * @param axisStepSize the step size. A value less than or equal to 0
     *        disables x axis grid lines.
     */
    public void setXAxisStepSize(double axisStepSize) {
        this.xAxisStepSize = axisStepSize;
    }

    /**
     * Sets the step size to use when drawing the grid lines for the y axis.
     *
     * @param axisStepSize the step size. A value less than or equal to 0
     *        disables y axis grid lines.
     */
    public void setYAxisStepSize(double axisStepSize) {
        this.yAxisStepSize = axisStepSize;
    }

    /**
     * Draws the gridlines for the plot.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     *
     * @see #drawRangeGridlines(Graphics2D, Rectangle2D, List)
     */
    protected void drawDomainGridlines(Graphics2D g2, Rectangle2D dataArea) {
        // TODO: Implement proper support for x axis step size?
        if (this.xAxisStepSize > 0) {
            super.drawDomainGridlines(g2, dataArea);
        }
    }

    /**
     * Draws the gridlines for the plot.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param ticks  the ticks.
     *
     * @see #drawDomainGridlines(Graphics2D, Rectangle2D)
     */
    protected void drawRangeGridlines(Graphics2D g2, Rectangle2D dataArea,
                                      List ticks) {
        // draw the range grid lines, if any...
        if (isRangeGridlinesVisible()) {
            Stroke gridStroke = getRangeGridlineStroke();
            Paint gridPaint = getRangeGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                ValueAxis axis = getRangeAxis();
                if (axis != null) {
                    double lower = axis.getRange().getLowerBound();
                    double upper = axis.getRange().getUpperBound();
                    double y = lower;
                    while (y <= upper) {
                        Paint paint = gridPaint;
                        if ((y == lower || y == upper)
                        		&& gridPaint instanceof Color) {
                            Color c = (Color) gridPaint;
                            paint = new Color(c.getRed(), c.getGreen(),
                                              c.getBlue(), c.getAlpha() / 3);
                        }
                        try {
                            setRangeGridlinePaint(paint);
                            getRenderer().drawRangeGridline(g2, this,
                            		getRangeAxis(), dataArea, y);
                        }
                        finally {
                            setRangeGridlinePaint(gridPaint);
                        }
                        y += this.yAxisStepSize;
                    }
                }
            }
        }
    }
}
