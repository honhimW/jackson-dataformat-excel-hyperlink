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

import com.fasterxml.jackson.databind.JavaType;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Table {

    private final ColumnPointer _pointer;
    private final String _name;
    private final JavaType _type;
    private final List<Column> _columns = new ArrayList<>();

    public Table(final ColumnPointer _pointer, final String _name, final JavaType _type) {
        this._pointer = _pointer;
        this._name = _name;
        this._type = _type;
    }

    public List<Column> getColumns() {
        return _columns;
    }

    public boolean matches(final ColumnPointer other) {
        return _pointer.equals(other);
    }

    public ColumnPointer getPointer() {
        return _pointer;
    }

    public String getName() {
        if (_name.isEmpty()) {
            return _type.getRawClass().getSimpleName();
        }
        return _name;
    }

    public JavaType getType() {
        return _type;
    }
}
