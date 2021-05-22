package br.com.concepting.framework.controller.form.constants;

import br.com.concepting.framework.controller.types.ScopeType;

/**
 * Class that defines the constants used in the manipulation of forms.
 *
 * @author fvilarinho
 * @since 3.1.0
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
@SuppressWarnings("javadoc")
public abstract class ActionFormConstants{
    public static final String ACTION_ATTRIBUTE_ID = "action";
    public static final String DATASET_ATTRIBUTE_ID = "dataset";
    public static final String DATASET_SCOPE_ATTRIBUTE_ID = "datasetScope";
    public static final String DATASET_START_INDEX_ATTRIBUTE_ID = "datasetStartIndex";
    public static final String DATASET_END_INDEX_ATTRIBUTE_ID = "datasetEndIndex";
    public static final String FORWARD_ATTRIBUTE_ID = "forward";
    public static final String DEFAULT_ACTION_SERVLET_FILE_EXTENSION = ".ui";
    public static final String DEFAULT_FORWARD_ID = "index";
    public static final ScopeType DEFAULT_DATASET_SCOPE_TYPE = ScopeType.MODEL;
}