/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.model.impl.struct;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPToolTipObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.DBPositiveNumberTransformer;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.utils.CommonUtils;

/**
 * AbstractAttribute
 */
public abstract class AbstractAttribute implements DBSAttributeBase, DBSTypedObjectExt2, DBSTypedObjectExt3, DBPToolTipObject, DBPNamedObject2
{
    protected String name;
    protected int valueType;
    protected long maxLength;
    protected boolean required;
    protected boolean autoGenerated;
    protected int scale;
    protected int precision;
    protected String typeName;
    protected int ordinalPosition;

    protected AbstractAttribute()
    {
    }

    // Copy constructor
    protected AbstractAttribute(DBSAttributeBase source)
    {
        this(
            source.getName(),
            source.getTypeName(),
            source.getTypeID(),
            source.getOrdinalPosition(),
            source.getMaxLength(),
            source.getScale(),
            source.getPrecision(),
            source.isRequired(),
            source.isAutoGenerated());
    }

    protected AbstractAttribute(
            String name,
            String typeName,
            int valueType,
            int ordinalPosition,
            long maxLength,
            Integer scale,
            Integer precision,
            boolean required,
            boolean autoGenerated)
    {
        this.name = name;
        this.valueType = valueType;
        this.maxLength = maxLength;
        this.scale = scale == null ? -1 : scale;
        this.precision = precision == null ? - 1 : precision;
        this.required = required;
        this.autoGenerated = autoGenerated;
        this.typeName = typeName;
        this.ordinalPosition = ordinalPosition;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 10, valueTransformer = DBObjectNameCaseTransformer.class)
    public String getName()
    {
        return name;
    }

    public void setName(String columnName)
    {
        this.name = columnName;
    }

    @Property(viewable = true, order = 15, valueRenderer = DBPositiveNumberTransformer.class)
    public int getOrdinalPosition()
    {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition)
    {
        this.ordinalPosition = ordinalPosition;
    }

    @Override
    @Property(viewable = true, order = 20)
    public String getTypeName()
    {
        return typeName;
    }

    @Override
    public String getFullTypeName() {
        return DBUtils.getFullTypeName(this);
    }

    @Override
    public void setTypeName(String typeName) throws DBException
    {
        this.typeName = typeName;
    }

    @Override
    public void setFullTypeName(String fullTypeName) throws DBException {
        String plainTypeName;
        int divPos = fullTypeName.indexOf("(");
        if (divPos == -1) {
            plainTypeName = fullTypeName;
            validateTypeName(plainTypeName);
        } else {
            plainTypeName = fullTypeName.substring(0, divPos);
            validateTypeName(plainTypeName);
            int divPos2 = fullTypeName.indexOf(')', divPos);
            if (divPos2 != -1) {
                String modifiers = fullTypeName.substring(divPos + 1, divPos2);
                int divPos3 = modifiers.indexOf(',');
                if (divPos3 == -1) {
                    maxLength = precision = CommonUtils.toInt(modifiers, -1);
                } else {
                    precision= CommonUtils.toInt(modifiers.substring(0, divPos3).trim());
                    scale = CommonUtils.toInt(modifiers.substring(divPos3 + 1).trim());
                }
            }
        }
        setTypeName(plainTypeName);
    }

    protected void validateTypeName(String typeName) throws DBException {

    }

    @Override
    public int getTypeID()
    {
        return valueType;
    }

    public void setValueType(int valueType)
    {
        this.valueType = valueType;
    }

    @Override
    @Property(viewable = true, order = 40)
    public long getMaxLength()
    {
        return maxLength;
    }

    @Override
    public void setMaxLength(long maxLength)
    {
        this.maxLength = maxLength;
    }

    @Override
    @Property(viewable = false, valueRenderer = DBPositiveNumberTransformer.class, order = 41)
    public Integer getScale()
    {
        return scale == -1 ? null : scale;
    }

    @Override
    public void setScale(Integer scale)
    {
        this.scale = scale == null ? -1 : scale;
    }

    @Override
    @Property(viewable = false, valueRenderer = DBPositiveNumberTransformer.class, order = 42)
    public Integer getPrecision()
    {
        return precision == -1 ? null : precision;
    }

    @Override
    public void setPrecision(Integer precision)
    {
        this.precision = precision == null ? -1 : precision;
    }

    @Property(viewable = true, order = 50)
    public boolean isRequired()
    {
        return required;
    }

    @Override
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    @Property(viewable = true, order = 55)
    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    @Override
    public long getTypeModifiers() {
        return 0;
    }

    @Nullable
    public String getDescription()
    {
        return null;
    }

    public boolean isPersisted()
    {
        return true;
    }

    @Override
    public String getObjectToolTip() {
        return getFullTypeName();
    }

    @Override
    public String toString() {
        return name + ", type=" + typeName + ", pos=" + ordinalPosition;
    }

    protected void onChangeDataType(DBSTypedObject oldType, DBSTypedObject newType) {
        if (oldType != null && newType != null) {
            DBPDataKind newTypeDataKind = newType.getDataKind();
            if (oldType.getDataKind() != newTypeDataKind) {
                long maxLength = newType.getMaxLength();
                if ((newTypeDataKind == DBPDataKind.STRING || newTypeDataKind == DBPDataKind.CONTENT) && maxLength > this.maxLength) {
                    this.maxLength = 100;
                } else {
                    this.maxLength = CommonUtils.toInt(maxLength, -1);
                }
                this.precision = CommonUtils.toInt(newType.getPrecision(), -1);
                this.scale = CommonUtils.toInt(newType.getScale(), -1);
            }
        }
    }

}
