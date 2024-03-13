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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import io.github.honhimw.jackson.dataformat.hyper.BookMappingIterator;
import io.github.honhimw.jackson.dataformat.hyper.HyperFactory;
import io.github.honhimw.jackson.dataformat.hyper.HyperMapper;
import io.github.honhimw.jackson.dataformat.hyper.deser.BookParser;
import io.github.honhimw.jackson.dataformat.hyper.schema.Column;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import io.github.honhimw.jackson.dataformat.hyper.schema.Table;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

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
        HyperSchema schema = mapper.sheetSchemaFor(Person.class);
        schema.filter(column -> !StringUtils.equals(column.getName(), "gender"));
        schema.filter(column -> !StringUtils.equals(column.getName(), "id"));

        schema.editColumns(columns -> {
            Column remove = columns.remove(1);
            columns.add(remove);
            schema.afterPropertySet();
        });

        byte[] bytes = mapper.writer(schema).writeValueAsBytes(Person.VALUES);
        Workbook sheets = WorkbookFactory.create(new ByteArrayInputStream(bytes));
        Sheet sheetAt = sheets.getSheetAt(0);
        SheetPrinter.print(sheetAt);
//        Row row = sheetAt.getRow(0);
//        Iterator<Cell> cellIterator = row.cellIterator();
//        while (cellIterator.hasNext()) {
//            Cell next = cellIterator.next();
//            String title = next.getStringCellValue();
//            System.out.print(title);
//            System.out.print(" ");
//            Assertions.assertFalse(StringUtils.equals("gender", title));
//        }
    }

    @Test
    @SneakyThrows
    public void editTables() {
        Workbook _workbook = new XSSFWorkbook();
        List<String> sheetNames = Arrays.asList("A", "B", "C", "D");
        AtomicInteger i = new AtomicInteger(0);
        {
            HyperMapper mapper = new HyperMapper(new HyperFactory(() -> _workbook));
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.enable(BookParser.Feature.REORDER_BY_COLUMN_NAME);
            mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

            for (final String sheetName : sheetNames) {
                HyperSchema schema = mapper.sheetSchemaFor(SimpleEntity.class);
                schema.editColumns(columns -> columns.sort((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 1)));
                schema.editTables(tablesEditor -> {
                    Table mainTable = tablesEditor.getMainTable();
                    mainTable.setName(sheetName);
                });
                Collection<SimpleEntity> generate = MockUtils.generate(SimpleEntity.class, 4);
                generate.forEach(simpleEntity -> simpleEntity.setId(i.getAndIncrement()));
                byte[] bytes = mapper.writer(schema).writeValueAsBytes(generate);
                Workbook sheets = WorkbookFactory.create(new ByteArrayInputStream(bytes));
            }
            _workbook.close();
        }

        SheetPrinter.print(_workbook);
        {
            HyperMapper mapper = new HyperMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.enable(BookParser.Feature.REORDER_BY_COLUMN_NAME);
            for (final String tableName : sheetNames) {
                HyperSchema schema = mapper.sheetSchemaFor(SimpleEntity.class);
                schema.editTables(tables -> {
                    Table mainTable = tables.getMainTable();
                    mainTable.setName(tableName);
                });
                try (BookMappingIterator<SimpleEntity> iterator = mapper.reader(schema).forType(SimpleEntity.class).readValues(_workbook)) {
                    List<SimpleEntity> simpleEntities = iterator.readAll();
                    simpleEntities.forEach(System.out::println);
                }
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleEntity implements Serializable {

        private Integer id;

        private Integer city;

        private Boolean gender;

        private String name;

    }

}
