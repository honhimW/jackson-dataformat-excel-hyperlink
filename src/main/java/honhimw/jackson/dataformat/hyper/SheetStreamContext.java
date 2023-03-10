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

package honhimw.jackson.dataformat.hyper;

import com.fasterxml.jackson.core.JsonStreamContext;
import honhimw.jackson.dataformat.hyper.schema.ColumnPointer;
import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import org.apache.poi.ss.util.CellAddress;

public abstract class SheetStreamContext extends JsonStreamContext {

    protected static final int INITIAL_INDEX = -1;
    protected final HyperSchema _schema;
    protected int _size;

    protected SheetStreamContext(final int type, final HyperSchema schema) {
        super(type, INITIAL_INDEX);
        _schema = schema;
    }

    public static SheetStreamContext createRootContext(final HyperSchema schema) {
        return new RootContext(schema);
    }

    @Override
    public SheetStreamContext getParent() {
        return null;
    }

    @SuppressWarnings("java:S1172") // Unused method parameters should be removed
    public SheetStreamContext getParent(final Matcher matcher) {
        return null;
    }

    @Override
    public String getCurrentName() {
        return null;
    }

    public void setCurrentName(final String name) {
    }

    public SheetStreamContext clearAndGetParent() {
        return getParent();
    }

    public SheetStreamContext createChildArrayContext() {
        return createChildArrayContext(-1);
    }

    public SheetStreamContext createChildArrayContext(final int size) {
        return new ArrayContext(this, size);
    }

    public SheetStreamContext createChildObjectContext(Object forValue) {
        return new ObjectContext(this, forValue);
    }

    public int size() {
        return _size;
    }

    public abstract int getRow();

    public abstract int getColumn();

    public abstract void writeValue();

    public CellAddress currentReference() {
        return new CellAddress(getRow(), getColumn());
    }

    public abstract ColumnPointer currentPointer();

    public ColumnPointer relativePointer(final ColumnPointer other) {
        return getParent().currentPointer().relativize(other);
    }

    interface StepAware {

        int DEFAULT_STEP = 1;

        int getStep();

        void setStep(int step);
    }

    interface Matcher {

        boolean matches(SheetStreamContext context);
    }

    static final class RootContext extends SheetStreamContext implements StepAware {

        private int _step = DEFAULT_STEP;

        RootContext(final HyperSchema schema) {
            super(TYPE_ROOT, schema);
        }

        @Override
        public ColumnPointer currentPointer() {
            return ColumnPointer.empty();
        }

        @Override
        public int getRow() {
            return _schema.getDataRow() + _index;
        }

        @Override
        public int getColumn() {
            return _schema.getOriginColumn() + _index;
        }

        @Override
        public void writeValue() {
            _index += _step;
            _step = DEFAULT_STEP;
            _size = _index + 1;
        }

        @Override
        public int getStep() {
            return _step;
        }

        @Override
        public void setStep(final int step) {
            _step = step;
        }
    }

    abstract static class ChildContext extends SheetStreamContext {

        protected final SheetStreamContext _parent;

        ChildContext(final int type, final SheetStreamContext parent) {
            super(type, parent._schema);
            _parent = parent;
        }

        public abstract Range contentRows();

        @Override
        public SheetStreamContext getParent() {
            return _parent;
        }

        @Override
        public SheetStreamContext getParent(final Matcher matcher) {
            return matcher.matches(_parent) ? _parent : _parent.getParent(matcher);
        }

        @Override
        public SheetStreamContext clearAndGetParent() {
            _parent._size += _size - 1;
            return super.clearAndGetParent();
        }
    }


    static final class ArrayContext extends ChildContext implements StepAware {

        private int _step = DEFAULT_STEP;

        ArrayContext(final SheetStreamContext parent, final int size) {
            super(TYPE_ARRAY, parent);
            _size = size;
        }

        @Override
        public ColumnPointer currentPointer() {
            if (_parent.inRoot()) {
                return _parent.currentPointer();
            }
            return _parent.currentPointer().resolveArray();
        }

        @Override
        public Range contentRows() {
            return new Range(0, 0);
        }

        @Override
        public int getRow() {
            return _parent.getRow() + _index;
        }

        @Override
        public int getColumn() {
            return _parent.getColumn();
        }

        @Override
        public void writeValue() {
            _index += _step;
            _step = DEFAULT_STEP;
            _size = _index + 1;
        }

        @Override
        public SheetStreamContext clearAndGetParent() {
            final SheetStreamContext parent = super.getParent(StepAware.class::isInstance);
            if (parent != null) {
                ((StepAware) parent).setStep(_step + getRow() - parent.getRow());
            }
            return super.clearAndGetParent();
        }

        @Override
        public int getStep() {
            return _step;
        }

        @Override
        public void setStep(final int step) {
            _step = step;
        }
    }

    static final class ObjectContext extends ChildContext {

        private String _name;

        private final Object _currentValue;

        private Range range;

        ObjectContext(final SheetStreamContext parent, final Object currentValue) {
            super(TYPE_OBJECT, parent);
            this._currentValue = currentValue;
        }

        @Override
        public String getCurrentName() {
            return _name;
        }

        @Override
        public void setCurrentName(final String name) {
            _name = name;
        }

        @Override
        public Object getCurrentValue() {
            return _currentValue;
        }

        @Override
        public ColumnPointer currentPointer() {
            return _parent.currentPointer().resolve(_name);
        }

        @Override
        public Range contentRows() {
            return range;
        }

        @Override
        public int getRow() {
            int row = _parent.getRow();
            range = new Range(row, row);
            return row;
        }

        @Override
        public int getColumn() {
            return _schema.getOriginColumn() + _index;
        }

        @Override
        public void writeValue() {
            _index = _schema.columnIndexOfCurrentSheet(currentPointer());
            if (_size == 0) {
                _size = 1;
            }
        }
    }
}
