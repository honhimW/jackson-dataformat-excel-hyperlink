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

package io.github.honhimw.jackson.dataformat.hyper.temp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.github.honhimw.jackson.dataformat.hyper.HyperMapper;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

/**
 * @author hon_him
 * @since 2023-03-25
 */

public class SchemaEditTests {

    HyperMapper mapper;

    @BeforeEach
    public void prepare() {
        mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Test
    @SneakyThrows
    public void edit() {
        HyperSchema columns = mapper.sheetSchemaFor(Person.class);
        columns.filter(column -> !StringUtils.equals(column.getName(), "gender"));
        columns.filter(column -> !StringUtils.equals(column.getName(), "id"));
        byte[] bytes = mapper.writer(columns).writeValueAsBytes(Person.VALUES);
        Workbook sheets = WorkbookFactory.create(new ByteArrayInputStream(bytes));
        Sheet sheetAt = sheets.getSheetAt(0);
        Row row = sheetAt.getRow(0);
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell next = cellIterator.next();
            String title = next.getStringCellValue();
            Assertions.assertFalse(StringUtils.equals("gender", title));
            Assertions.assertFalse(StringUtils.equals("id", title));
        }
    }

}
