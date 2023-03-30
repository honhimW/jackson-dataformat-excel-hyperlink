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

package io.github.honhimw.jackson.dataformat.hyper.poi.ooxml;

import io.github.honhimw.jackson.dataformat.hyper.deser.SheetReaderTestBase;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.junit.jupiter.api.Test;

class SSMLBookReaderTest extends SheetReaderTestBase {

    @Test
    void testTransitional() throws Exception {
        final SSMLWorkbook workbook = SSMLWorkbook.create(transitionalSource);
        final PackagePart worksheetPart = workbook.getWorksheetPartAt(0);
        reader = new SSMLBookReader(worksheetPart, workbook);
        testSheetReader();
    }

    @Test
    void testStrict() throws Exception {
        final SSMLWorkbook workbook = SSMLWorkbook.create(strictSource);
        final PackagePart worksheetPart = workbook.getWorksheetPartAt(0);
        reader = new SSMLBookReader(worksheetPart, workbook);
        testSheetReader();
    }
}
