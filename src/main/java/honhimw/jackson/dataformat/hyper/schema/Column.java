/*
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

package honhimw.jackson.dataformat.hyper.schema;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import java.util.StringJoiner;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class Column {

    private final ColumnPointer _pointer;
    private final String _name;
    private final JavaType _type;
    private final BeanProperty _beanProperty;
    private final boolean _leaf;
    private Table _table;

    public Column(final ColumnPointer pointer, final String name, final JavaType type, BeanProperty beanProperty) {
        this(pointer, name, type, beanProperty, true);
    }

    public Column(final ColumnPointer pointer, final String name, final JavaType type, BeanProperty beanProperty, boolean leaf) {
        this._pointer = pointer;
        this._name = name;
        this._type = type;
        this._beanProperty = beanProperty;
        this._leaf = leaf;
    }

    public boolean isLeaf() {
        return _leaf;
    }

    public ColumnPointer getPointer() {
        return _pointer;
    }

    public boolean matches(final ColumnPointer other) {
        if (_pointer.equals(other)) {
            return true;
        } else if (isArray()) {
            return _pointer.getParent().equals(other);
        }
        return false;
    }

    public String getName() {
        if (_name.isEmpty()) {
            return _pointer.toString();
        }
        return _name;
    }

    public JavaType getType() {
        if (isArray()) {
            return _type.getContentType();
        }
        return _type;
    }

    public BeanProperty getProp() {
        return _beanProperty;
    }

    public Table getTable() {
        return _table;
    }

    public void setTable(final Table table) {
        this._table = table;
    }

    public boolean isArray() {
        return _type.isArrayType() || _type.isCollectionLikeType();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Column.class.getSimpleName() + "[", "]")
            .add("pointer=" + _pointer)
            .add("name=" + _name)
            .add("type=" + _type)
            .toString();
    }
}
