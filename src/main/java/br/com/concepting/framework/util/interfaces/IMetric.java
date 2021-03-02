package br.com.concepting.framework.util.interfaces;

/**
 * Interface that defines the basic implementation of a metric.
 *
 * @author fvilarinho
 * @since 1.0.0
 *
 * <pre>Copyright (C) 2007 Innovative Thinking.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.</pre>
 */
public interface IMetric{
    /**
     * Returns the value of the metric.
     *
     * @return Numeric value.
     */
    Double getValue();
    
    /**
     * Defines the value of the metric.
     *
     * @param value Numeric value.
     */
    void setValue(Double value);
    
    /**
     * Returns the unit of the metric.
     *
     * @return String that contains the unit.
     */
    String getUnit();
    
    /**
     * Defines the unit of the metric.
     *
     * @param unit String that contains the unit.
     */
    void setUnit(String unit);
}