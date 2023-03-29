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

package io.github.honhimw.jackson.dataformat.hyper.deser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.honhimw.jackson.dataformat.hyper.support.FixtureAs;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public class SheetReaderTestBase implements FixtureAs {

    protected final InputStream transitionalSource = fixtureAsStream("entries-headless.xlsx");
    protected final InputStream strictSource = fixtureAsStream("entries-headless-strict.xlsx");
    protected BookReader reader;

    protected void testSheetReader() throws Exception {
        assertNext(SheetToken.SHEET_DATA_START);
        assertNoRow();
        assertNoCellValue();
        for (int i = 0; i < 2; i++) {
            assertNext(SheetToken.ROW_START);
            assertRow();
            assertNoCellValue();
            for (int j = 0; j < 2; j++) {
                assertNext(SheetToken.CELL_VALUE);
                assertRow();
                assertCellValue();
            }
            assertNext(SheetToken.ROW_END);
            assertRow();
            assertNoCellValue();
        }
        assertNext(SheetToken.SHEET_DATA_END);
        assertExhausted();
        assertClose();
    }

    void assertNext(final SheetToken token) {
        assertThat(reader).hasNext();
        assertThat(reader.next()).isEqualTo(token);
    }

    void assertRow() {
        assertThat(reader.getRow()).isNotNegative();
    }

    void assertNoRow() {
        assertThat(reader.getRow()).isNegative();
    }

    void assertCellValue() {
        assertThat(reader.getReference()).isNotNull();
        assertThat(reader.getCellValue()).isNotNull();
    }

    void assertNoCellValue() {
        assertThat(reader.getReference()).isNull();
        assertThat(reader.getCellValue()).isNull();
    }

    void assertExhausted() {
        assertThat(reader).isExhausted();
        assertThatThrownBy(reader::next).isInstanceOf(NoSuchElementException.class);
    }

    void assertClose() throws IOException {
        reader.close();
        assertThat(reader.isClosed()).isTrue();
    }
}
