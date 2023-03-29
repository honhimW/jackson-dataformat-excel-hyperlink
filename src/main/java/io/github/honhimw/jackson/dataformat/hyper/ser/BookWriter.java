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

package io.github.honhimw.jackson.dataformat.hyper.ser;

import io.github.honhimw.jackson.dataformat.hyper.schema.ColumnPointer;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellAddress;

public interface BookWriter extends AutoCloseable {

    SpreadsheetVersion getSpreadsheetVersion();

    void switchSheet(Class<?> type);

    void link(final Class<?> type, String value, int row);

    void setSchema(HyperSchema schema);

    void setReference(CellAddress reference);

    void currentValue(Object value);

    Cell getCell();

    void writeHeaders();

    void writeNumeric(double value);

    void writeString(String value);

    void writeBoolean(boolean value);

    void writeBlank();

    void adjustColumnWidth();

    void mergeScopedColumns(ColumnPointer pointer, int row, int size);

    void write(OutputStream out) throws IOException;

    @Override
    void close() throws IOException;

    boolean isDate1904();
}
