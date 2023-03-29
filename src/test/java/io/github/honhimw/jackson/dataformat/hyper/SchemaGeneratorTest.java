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

package io.github.honhimw.jackson.dataformat.hyper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import io.github.honhimw.jackson.dataformat.hyper.schema.generator.AnnotatedNameResolver;
import io.github.honhimw.jackson.dataformat.hyper.schema.generator.ColumnNameResolver;
import io.github.honhimw.jackson.dataformat.hyper.temp.ColumnCode;
import io.github.honhimw.jackson.dataformat.hyper.temp.NameOf;
import io.github.honhimw.jackson.dataformat.hyper.temp.Person;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import lombok.Data;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SchemaGeneratorTest {

    @TempDir
    Path tempDir;
    File out;
    Row row;

    @BeforeEach
    void setUp() throws Exception {
        out = tempDir.resolve("test.xlsx").toFile();
    }

    @Test
    void changeOriginAddress() throws Exception {
        final HyperMapper mapper = new HyperMapper().setOrigin("B2");
        mapper.writeValue(out, Person.VALUE);
        try (XSSFWorkbook workbook = new XSSFWorkbook(out)) {
            final XSSFSheet sheet = workbook.getSheetAt(0);
            row = sheet.getRow(0);
            assertThat(row).isNull();
            row = sheet.getRow(1);
            assertThat(row.getCell(0)).isNull();
            row = sheet.getRow(2);
            System.out.println();
            assertThat(row.getCell(0)).isNull();
        }
    }

    @Test
    void overwriteColumnNames() throws Exception {
        final HyperMapper mapper = new HyperMapper()
                .setColumnNameResolver(prop -> prop.getName().toUpperCase());
        mapper.writeValue(out, List.of(Person.VALUE), Person.class);
        try (XSSFWorkbook workbook = new XSSFWorkbook(out)) {
            final XSSFSheet sheet = workbook.getSheetAt(0);
            row = sheet.getRow(0);
            assertCellValue(0, "ID");
            assertCellValue(1, "HEIGHT");
        }
    }

    @Test
    void annotatedColumnNames() throws Exception {
        ColumnNameResolver byText = AnnotatedNameResolver.forValue(NameOf.class, ColumnCode::getText);
        final HyperMapper mapper = new HyperMapper()
                .setColumnNameResolver(byText);
        mapper.writeValue(out, null, AnnotatedEntity.class);
        try (XSSFWorkbook workbook = new XSSFWorkbook(out)) {
            final XSSFSheet sheet = workbook.getSheetAt(0);
            row = sheet.getRow(0);
            assertCellValue(0, ColumnCode.A.getText());
            assertCellValue(1, ColumnCode.B.getText());
        }
    }

    @Test
    void annotatedNameMustHaveAnnotation() throws Exception {
        ColumnNameResolver byText = AnnotatedNameResolver.forValue(NameOf.class, ColumnCode::getText);
        final HyperMapper mapper = new HyperMapper()
                .setColumnNameResolver(byText);
        assertThatThrownBy(() -> mapper.writeValue(out, null, MissingAnnotationEntity.class))
                .isInstanceOf(InvalidDefinitionException.class)
                .hasMessageContaining("Annotation `@%s` must not be null for property", NameOf.class.getSimpleName());
    }

    void assertCellValue(final int cellnum, String expected) {
        assertThat(row.getCell(cellnum).getStringCellValue()).isEqualTo(expected);
    }

    void assertCellValue(final int cellnum, double expected) {
        assertThat(row.getCell(cellnum).getNumericCellValue()).isEqualTo(expected);
    }

    @Data
    class AnnotatedEntity {
        @NameOf(ColumnCode.A)
        int a;
        @NameOf(ColumnCode.B)
        int b;
    }

    @Data
    class MissingAnnotationEntity {
        int a;
        @NameOf(ColumnCode.B)
        int b;
    }
}
