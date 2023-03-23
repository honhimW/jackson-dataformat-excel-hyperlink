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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.ss.util.CellAddress;

public abstract class BookStreamContext extends JsonStreamContext {

    protected static final int INITIAL_INDEX = -1;
    protected final HyperSchema _schema;
    protected int _size;

    protected BookStreamContext(final int type, final HyperSchema schema) {
        super(type, INITIAL_INDEX);
        _schema = schema;
    }

    public static BookStreamContext createRootContext(final HyperSchema schema) {
        return new RootContext(schema);
    }

    @Override
    public BookStreamContext getParent() {
        return null;
    }

    @SuppressWarnings("java:S1172") // Unused method parameters should be removed
    public BookStreamContext getParent(final Matcher matcher) {
        return null;
    }

    @Override
    public String getCurrentName() {
        return null;
    }

    public void setCurrentName(final String name) {
    }

    public BookStreamContext clearAndGetParent() {
        return getParent();
    }

    public BookStreamContext createChildArrayContext() {
        return createChildArrayContext(null, -1);
    }

    public BookStreamContext createChildArrayContext(final Object forValue, final int size) {
        return new ArrayContext(this, forValue, size);
    }

    public BookStreamContext createChildObjectContext(Object forValue) {
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

        boolean matches(BookStreamContext context);
    }

    static final class RootContext extends BookStreamContext implements StepAware {

        private int _step = DEFAULT_STEP;

        private final Map<Class<?>, AtomicInteger> _tableRows = new HashMap<>();

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

    abstract static class ChildContext extends BookStreamContext {

        protected final BookStreamContext _parent;

        ChildContext(final int type, final BookStreamContext parent) {
            super(type, parent._schema);
            _parent = parent;
        }

        @Override
        public BookStreamContext getParent() {
            return _parent;
        }

        @Override
        public BookStreamContext getParent(final Matcher matcher) {
            return matcher.matches(_parent) ? _parent : _parent.getParent(matcher);
        }

        @Override
        public BookStreamContext clearAndGetParent() {
            _parent._size += _size - 1;
            return super.clearAndGetParent();
        }

        protected Map<Class<?>, AtomicInteger> getTableRowsMap() {
            BookStreamContext context = this;
            while (context.getParent() != null) {
                context = context.getParent();
                if (context instanceof RootContext rootContext) {
                    return rootContext._tableRows;
                }
            }
            throw new IllegalStateException("not supposed to happen");
        }

    }

    static final class ArrayContext extends ChildContext implements StepAware {

        private int _step = DEFAULT_STEP;

        private final Object _currentValue;

        private final int row;

        ArrayContext(final BookStreamContext parent, final Object currentValue, final int size) {
            super(TYPE_ARRAY, parent);
            _size = size;
            this._currentValue = currentValue;
            AtomicInteger atomicInteger;

            if (getTableRowsMap().containsKey(List.class)) {
                atomicInteger = getTableRowsMap().get(List.class);
                atomicInteger.incrementAndGet();
            } else {
                atomicInteger = new AtomicInteger(_schema.getOriginRow());
                atomicInteger.incrementAndGet();
                getTableRowsMap().put(List.class, atomicInteger);
            }
            row = atomicInteger.get();
        }

        @Override
        public Object getCurrentValue() {
            return _currentValue;
        }

        @Override
        public ColumnPointer currentPointer() {
            if (_parent.inRoot()) {
                return _parent.currentPointer();
            }
            return _parent.currentPointer().resolveArray();
        }

        @Override
        public int getRow() {
            return row;
        }

        @Override
        public int getColumn() {
            return _schema.getOriginColumn() + _index;
        }

        @Override
        public void writeValue() {
            _index++;
        }

        @Override
        public BookStreamContext clearAndGetParent() {
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

        private final int row;

        ObjectContext(final BookStreamContext parent, final Object currentValue) {
            super(TYPE_OBJECT, parent);
            this._currentValue = currentValue;
            if (Objects.nonNull(_currentValue)) {
                AtomicInteger atomicInteger;

                if (getTableRowsMap().containsKey(_currentValue.getClass())) {
                    atomicInteger = getTableRowsMap().get(_currentValue.getClass());
                    atomicInteger.incrementAndGet();
                } else {
                    atomicInteger = new AtomicInteger(_schema.getDataRow());
                    getTableRowsMap().put(_currentValue.getClass(), atomicInteger);
                }
                row = atomicInteger.get();
            } else {
                row = -1;
            }
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
        public int getRow() {
            return row;
        }

        @Override
        public int getColumn() {
            return _index;
        }

        @Override
        public void writeValue() {
            _index = _schema.columnIndexOfCurrentSheet(_currentValue.getClass(), _name);
            if (_size == 0) {
                _size = 1;
            }
        }

    }
}
