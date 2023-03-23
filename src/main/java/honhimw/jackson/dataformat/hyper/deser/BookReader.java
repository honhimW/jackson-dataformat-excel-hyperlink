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

package honhimw.jackson.dataformat.hyper.deser;

import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellAddress;

import java.io.IOException;
import java.util.Iterator;

public interface BookReader extends AutoCloseable, Iterator<SheetToken> {

    SpreadsheetVersion getSpreadsheetVersion();

    boolean isDate1904();

    void setSchema(HyperSchema schema);

    CellAddress getReference();

    CellValue getCellValue();

    Cell getCell();

    int getRow();

    int getColumn();

    boolean isClosed();

    @Override
    void close() throws IOException;
}
