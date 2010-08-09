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
 * ------------
 * GXYPlot.java
 * ------------
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 13-Dec-2007 : Version 1 (DG);
 * 30-Jun-2008 : Added support for specifying the step size to use for
 *               grid lines (NT);
 *
 */

package org.jfree.eastwood;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

/**
 * A custom plot class (adds support for drawing background gradients and
 * specyfing the step size to use for grid lines).
 */
class GXYPlot extends XYPlot {

    /** The angle in radians. */
    private double angle;

    /** The factor for the start of the background gradient. */
    private double f0;

    /** The factor for the end of the background gradient. */
    private double f1;

    /** The step size to use when drawing the grid lines for the x axis. */
    private double xAxisStepSize;

    /** The step size to use when drawing the grid lines for the y axis. */
    private double yAxisStepSize;

    /**
    * A custom label generator.
    */
    static class LabelGenerator extends StandardXYItemLabelGenerator {
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
        public String generateLabel(XYDataset dataset, int series, int item) {
            if (predefinedLabels.containsKey(Integer.toString(series) + "_" + Integer.toString(item))){
                return (String)predefinedLabels.get(Integer.toString(series) + "_" + Integer.toString(item));
            } else {
                return "";
            }
        }
    }

    /**
     * Default constructor.
     */
    public GXYPlot() {
        this.angle = 0.0;
        this.f0 = 0.0;
        this.f1 = 1.0;
        this.xAxisStepSize = 0.0;
        this.yAxisStepSize = 0.0;
    }

    /**
     * Sets the angle (in radians).
     *
     * @param angle  the angle.
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Sets the factor for the beginning of the background gradient.
     *
     * @param f0  the factor.
     */
    public void setF0(double f0) {
        this.f0 = f0;
    }

    /**
     * Sets the factor for the end of the background gradient.
     *
     * @param f1  the factor.
     */
    public void setF1(double f1) {
        this.f1 = f1;
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
     * Draws the background for the plot.
     *
     * @param g2  the graphics device.
     * @param area  the area.
     */
    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        Paint p = getBackgroundPaint();
        if (p instanceof GradientPaint) {
            // do the transformation directly
            GradientPaint gp = (GradientPaint) p;
            double r = (this.f1 - this.f0) * area.getWidth();
            Point2D p0 = new Point2D.Double(area.getMinX() + this.f0
                    * area.getWidth(), area.getMaxY());
            Point2D p1 = new Point2D.Double(p0.getX() + r
                    * Math.cos(this.angle), p0.getY() - r
                    * Math.sin(this.angle));
            p = new GradientPaint(p0, gp.getColor1(), p1,
                    gp.getColor2());
        }
        g2.setPaint(p);
        g2.fill(area);
    }

    /**
     * Draws the gridlines for the plot, if they are visible.
     *
     * @param g2  the graphics device.
     * @param dataArea  the data area.
     * @param ticks  the ticks.
     *
     * @see #drawRangeGridlines(Graphics2D, Rectangle2D, List)
     */
    protected void drawDomainGridlines(Graphics2D g2, Rectangle2D dataArea,
                                       List ticks) {

        // no renderer, no gridlines...
        if (getRenderer() == null) {
            return;
        }

        // draw the domain grid lines, if any...
        if (isDomainGridlinesVisible() && this.xAxisStepSize > 0) {
            Stroke gridStroke = getDomainGridlineStroke();
            Paint gridPaint = getDomainGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                ValueAxis axis = getDomainAxis();
                if (axis != null) {
                    double lower = axis.getRange().getLowerBound();
                    double upper = axis.getRange().getUpperBound();
                    double x = lower;
                    while (x <= upper) {
                        Paint paint = gridPaint;
                        if ((x == lower || x == upper)
                        		&& gridPaint instanceof Color) {
                            Color c = (Color) gridPaint;
                            paint = new Color(c.getRed(), c.getGreen(),
                                              c.getBlue(), c.getAlpha() / 3);
                        }
                        try {
                            setDomainGridlinePaint(paint);
                            getRenderer().drawDomainGridLine(g2, this,
                            		getDomainAxis(), dataArea, x);
                        }
                        finally {
                            setDomainGridlinePaint(gridPaint);
                        }
                        x += this.xAxisStepSize;
                    }
                }
            }
        }
    }

    /**
     * Draws the gridlines for the plot's primary range axis, if they are
     * visible.
     *
     * @param g2  the graphics device.
     * @param area  the data area.
     * @param ticks  the ticks.
     *
     * @see #drawDomainGridlines(Graphics2D, Rectangle2D, List)
     */
    protected void drawRangeGridlines(Graphics2D g2, Rectangle2D area,
                                      List ticks) {

        // no renderer, no gridlines...
        if (getRenderer() == null) {
            return;
        }

        // draw the range grid lines, if any...
        if (isRangeGridlinesVisible() && this.yAxisStepSize > 0) {
            Stroke gridStroke = getRangeGridlineStroke();
            Paint gridPaint = getRangeGridlinePaint();
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
                    getRenderer().drawRangeLine(g2, this, getRangeAxis(),
                            area, y, paint, gridStroke);
                    y += this.yAxisStepSize;
                }
            }
        }
    }
}
