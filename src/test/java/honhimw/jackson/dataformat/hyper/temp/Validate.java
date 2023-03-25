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

package honhimw.jackson.dataformat.hyper.temp;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import honhimw.jackson.dataformat.hyper.HyperMapper;
import honhimw.jackson.dataformat.hyper.deser.CellValue;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * @author hon_him
 * @since 2023-03-25
 */

public class Validate {

    @Test
    @SneakyThrows
    public void validate() {
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.acceptReadVisitor(ValidateBookReadVisitor::new);
        File file = new File("E:\\temp\\person.xlsx");
        mapper.writeValue(file, Person.VALUES, Person.class);

        Person person = mapper.readValue(file, Person.class);
        System.out.println(person.toString());
    }

    public static class ValidateBookReadVisitor extends BookReadVisitor {

        private final List<String> validateExceptions = new ArrayList<>();

        public ValidateBookReadVisitor(final BookReadVisitor writeVisitor) {
            super(writeVisitor);
        }

        @Override
        public void visitBook(final Workbook workbook, final HyperSchema schema) {
            super.visitBook(workbook, schema);
        }

        @Override
        public SheetReadVisitor visitSheet(final Sheet sheet) {
            SheetReadVisitor sheetReadVisitor = super.visitSheet(sheet);
            return new ValidateSheetReaderVisitor(sheetReadVisitor, validateExceptions);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if (CollectionUtils.isNotEmpty(validateExceptions)) {
                validateExceptions.forEach(System.out::println);
                throw new IllegalArgumentException("validate failed");
            }

        }
    }

    public static class ValidateSheetReaderVisitor extends SheetReadVisitor {
        private final List<String> _es;

        public ValidateSheetReaderVisitor(final SheetReadVisitor readVisitor, List<String> es) {
            super(readVisitor);
            _es = es;
        }

        @Override
        public RowReadVisitor visitRow(final Row row) {
            RowReadVisitor rowReadVisitor = super.visitRow(row);
            return new ValidateRowReaderVisitor(rowReadVisitor, _es);
        }
    }

    public static class ValidateRowReaderVisitor extends RowReadVisitor {

        public static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        private static final Class<?>[] GROUPS = new Class[0];
        private final List<String> _es;

        public ValidateRowReaderVisitor(final RowReadVisitor _readVisitor, List<String> es) {
            super(_readVisitor);
            _es = es;
        }

        @Override
        public CellValue visitCell(final Cell cell, final Column column) {
            CellValue cellValue = super.visitCell(cell, column);

            return Optional.ofNullable(column)
                .map(Column::getTable)
                .map(Table::getType)
                .map(javaType -> {
                    BeanProperty prop = column.getProp();
                    Set<? extends ConstraintViolation<?>> validResult = validator.validateValue(
                        javaType.getRawClass(), prop.getName(), cellValue.getValue(), GROUPS);
                    if (CollectionUtils.isNotEmpty(validResult)) {
                        String msg = validResult.stream().map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(","));
                        _es.add(String.format("%d:%d: %s", cell.getRowIndex() + 1, cell.getColumnIndex() + 1, msg));
                        return CellValue.BLANK;
                    }
                    return null;
                }).orElse(cellValue);
        }
    }


}
