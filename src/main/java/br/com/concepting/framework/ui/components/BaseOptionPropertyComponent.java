package br.com.concepting.framework.ui.components;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import br.com.concepting.framework.exceptions.InternalErrorException;
import br.com.concepting.framework.model.BaseModel;
import br.com.concepting.framework.model.helpers.ModelInfo;
import br.com.concepting.framework.model.util.ModelUtil;
import br.com.concepting.framework.util.PropertyUtil;
import br.com.concepting.framework.util.helpers.PropertyInfo;
import br.com.concepting.framework.util.types.ComponentType;

/**
 * Class that defines the basic implementation for a option component.
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
public abstract class BaseOptionPropertyComponent extends BasePropertyComponent{
	private static final long serialVersionUID = 8280294646254910196L;

	private Boolean selected    = null;
	private Object  optionValue = null;

	/**
	 * Returns the instance that contains the value of the option.
	 * 
	 * @return Instance that contains the value of the option.
	 */
	public Object getOptionValue(){
		return this.optionValue;
	}

	/**
	 * Defines the instance that contains the value of the option.
	 * 
	 * @param optionValue Instance that contains the value of the option.
	 */
	public void setOptionValue(Object optionValue){
		this.optionValue = optionValue;
	}

	/**
	 * Indicates if the option is selected.
	 * 
	 * @return True/False.
	 */
	protected Boolean isSelected(){
		return this.selected;
	}

	/**
	 * Defines if the option is selected.
	 * 
	 * @param selected True/False.
	 */
	protected void setSelected(Boolean selected){
		this.selected = selected;
	}

	/**
	 * @see br.com.concepting.framework.ui.components.BasePropertyComponent#getFormattedValue()
	 */
	protected String getFormattedValue() throws InternalErrorException{
		if(this.optionValue != null){
			PropertyInfo propertyInfo = getPropertyInfo();

			if(propertyInfo != null){
				Boolean isModel = propertyInfo.isModel();

				if(isModel == null || !isModel){
					try{
						this.optionValue = PropertyUtil.getValue(this.optionValue, propertyInfo.getId());
					}
					catch(Throwable e){
					}
				}
			}
		}

		if(this.optionValue != null){
			if(PropertyUtil.isModel(this.optionValue)){
				BaseModel model = (BaseModel)this.optionValue;

				try{
					return ModelUtil.toIdentifierString(model);
				}
				catch(Throwable e){
				}
			}
			else if(PropertyUtil.isEnum(this.optionValue))
				return ((Enum<?>)this.optionValue).name();

			return PropertyUtil.format(this.optionValue, getPattern(), useAdditionalFormatting(), getPrecision(), getCurrentLanguage());
		}

		return super.getFormattedValue();
	}

	/**
	 * @see br.com.concepting.framework.ui.components.BaseComponent#initialize()
	 */
	@SuppressWarnings("unchecked")
	protected void initialize() throws InternalErrorException{
		ComponentType componentType = getComponentType();

		if(componentType == null){
			componentType = ComponentType.OPTION;

			setComponentType(componentType);
		}

		super.initialize();

		PropertyInfo propertyInfo = getPropertyInfo();
		Object value = getValue();

		if(PropertyUtil.isCollection(value) && (propertyInfo != null && propertyInfo.isCollection())){
			if(value != null && this.optionValue != null){
				Collection<?> values = (Collection<?>)value;

				this.selected = (values.contains(this.optionValue));
			}
			else
				this.selected = false;
		}
		else if(PropertyUtil.isModel(this.optionValue) && !PropertyUtil.isModel(value)){
			try{
				Class<? extends BaseModel> modelClass = (Class<? extends BaseModel>)this.optionValue.getClass();
				ModelInfo modelInfo = ModelUtil.getInfo(modelClass);
				Collection<PropertyInfo> identityPropertiesInfo = modelInfo.getIdentityPropertiesInfo();

				if(identityPropertiesInfo != null && identityPropertiesInfo.size() == 1){
					PropertyInfo identityPropertyInfo = identityPropertiesInfo.iterator().next();
					Object identityValue = PropertyUtil.getValue(this.optionValue, identityPropertyInfo.getId());

					this.selected = (identityValue != null ? (value != null && value.equals(identityValue)) : false);
				}
				else
					this.selected = false;
			}
			catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException | IllegalArgumentException | InstantiationException | ClassNotFoundException | NoSuchFieldException e){
				this.selected = false;
			}
		}
		else if(this.optionValue != null)
			this.selected = (value != null && value.equals(this.optionValue));
		else if(this.isBoolean() != null && this.isBoolean())
			this.selected = (value != null && Boolean.valueOf(value.toString()));

		if(this.selected == null)
			this.selected = false;
	}

	/**
	 * @see br.com.concepting.framework.ui.components.BaseComponent#renderAttributes()
	 */
	protected void renderAttributes() throws InternalErrorException{
		super.renderAttributes();

		renderSelectionAttribute();
	}

	/**
	 * Renders the selection attribute of the option.
	 * 
	 * @throws InternalErrorException Occurs when was not possible to render.
	 */
	protected void renderSelectionAttribute() throws InternalErrorException{
		Boolean selected = isSelected();

		if(selected != null && selected)
			print(" checked");
	}

	/**
	 * @see br.com.concepting.framework.ui.components.BaseActionFormComponent#renderLabelBody()
	 */
	protected void renderLabelBody() throws InternalErrorException{
		Boolean showLabel = showLabel();
		String label = getLabel();

		if(showLabel != null && showLabel && label != null && label.length() > 0){
			if(this.optionValue != null)
				println(label);
			else
				super.renderLabelBody();
		}
	}

	/**
	 * @see br.com.concepting.framework.ui.components.BasePropertyComponent#clearAttributes()
	 */
	protected void clearAttributes() throws InternalErrorException{
		super.clearAttributes();

		setSelected(null);
		setOptionValue(null);
	}
}