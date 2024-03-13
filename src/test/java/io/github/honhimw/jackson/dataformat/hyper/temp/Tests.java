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

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.github.honhimw.jackson.dataformat.hyper.BookMappingIterator;
import io.github.honhimw.jackson.dataformat.hyper.ExcelDateModule;
import io.github.honhimw.jackson.dataformat.hyper.HyperGenerator;
import io.github.honhimw.jackson.dataformat.hyper.HyperMapper;
import io.github.honhimw.jackson.dataformat.hyper.deser.BookParser;
import io.github.honhimw.jackson.dataformat.hyper.deser.CellValue;
import io.github.honhimw.jackson.dataformat.hyper.schema.Column;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import io.github.honhimw.jackson.dataformat.hyper.schema.Table;
import io.github.honhimw.jackson.dataformat.hyper.schema.generator.ColumnNameResolver;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.RowReadVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.RowWriteVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.SheetWriteVisitor;
import io.github.honhimw.jackson.dataformat.hyper.temp.Person.Ext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.*;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

/**
 * @author hon_him
 * @since 2023-03-06
 */

public class Tests {

    File file;

    @BeforeEach
    @SneakyThrows
    public void tempFile() {
        file = File.createTempFile("hyper", "tests");
    }

    @Test
    @SneakyThrows
    public void excelMapper() {
        List<Person> origin = new ArrayList<>(MockUtils.generate(Person.class, 10));
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setColumnNameResolver(prop -> {
            ColumnName annotation = prop.getAnnotation(ColumnName.class);
            if (Objects.nonNull(annotation)) {
                if (StringUtils.isNotBlank(annotation.value())) {
                    return annotation.value();
                }
            }
            return ColumnNameResolver.DEFAULT.resolve(prop);
        });
        origin.forEach(person -> person.setProperties(Arrays.asList("hello", "world")));
        List<Object> p2 = new ArrayList<>(MockUtils.generate(Ext.class,
            ThreadLocalRandom.current().nextInt(2,5)));
        p2.add("hello");
        p2.add(123);
        p2.add(Arrays.asList("a", "b"));
        origin.forEach(person -> person.setProperties2(p2));
        origin.forEach(person -> person.setName(null));
        mapper.writeValue(file, origin, Person.class);

        List<Person> people = mapper.readValues(file, Person.class);
        for (int i = 0; i < 10; i++) {
            System.out.println(people.get(i));
            System.out.println(origin.get(i).equals(people.get(i)));
        }
        mapper.writeValue(file, origin, Person.class);

        people = mapper.readValues(file, Person.class);
        for (int i = 0; i < 10; i++) {
            System.out.println(people.get(i));
            System.out.println(origin.get(i).equals(people.get(i)));
        }


    }

    @Test
    @SneakyThrows
    public void one() {
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        mapper.acceptWriteVisitor(bookWriteVisitor -> new BookWriteVisitor(bookWriteVisitor) {
            @Override
            public SheetWriteVisitor visitSheet(final Sheet sheet, final Table table) {
                SheetWriteVisitor sheetWriteVisitor = super.visitSheet(sheet, table);
                System.out.println("write sheet: " + sheet.getSheetName());
                SheetWriteVisitor sheetWriteVisitor1 = new SheetWriteVisitor(sheetWriteVisitor) {
                    @Override
                    public RowWriteVisitor visitRow(final Row row, final Object value) {
                        System.out.println("object: " + value);
                        return super.visitRow(row, value);
                    }
                };
                return sheetWriteVisitor1;
            }

            @Override
            public void visitEnd() {
                System.out.println("write done");
            }
        });
        mapper.acceptReadVisitor(bookReadVisitor -> new BookReadVisitor(bookReadVisitor) {
            List<Object> list = new ArrayList<>();

            @Override
            public SheetReadVisitor visitSheet(final Sheet sheet) {
                SheetReadVisitor sheetReadVisitor = super.visitSheet(sheet);
                System.out.println("read sheet: " + sheet.getSheetName());

                return new SheetReadVisitor(sheetReadVisitor) {
                    @Override
                    public RowReadVisitor visitRow(final Row row) {
                        list.add(row);
                        System.out.println("read row: " + row.getRowNum());
                        RowReadVisitor rowReadVisitor = super.visitRow(row);

                        return new RowReadVisitor(rowReadVisitor) {
                            @Override
                            public CellValue visitCell(final Cell cell, final Column column) {
                                CellValue cellValue = super.visitCell(cell, column);
                                System.out.println((Objects.isNull(column) ? cell.getColumnIndex() : column.getName()) + ": " + cellValue);
                                return cellValue;
                            }
                        };
                    }
                };
            }

            @Override
            public void visitEnd() {
                System.out.println("read rows number = " + list.size());
                super.visitEnd();
            }
        });
        mapper.writeValue(file, Arrays.asList(Person.VALUE), Person.class);
        Person person = mapper.readValue(file, Person.class);
        System.out.println(person.toString());
    }

    @Test
    @SneakyThrows
    public void inMemory() {
        List<Person> origin = new ArrayList<>(MockUtils.generate(Person.class, 10));
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        origin.forEach(person -> person.setProperties(Arrays.asList("hello", "world")));
        List<Object> p2 = new ArrayList<>(MockUtils.generate(Ext.class,
            ThreadLocalRandom.current().nextInt(2,5)));
        p2.add("hello");
        p2.add(123);
        p2.add(Arrays.asList("a", "b"));
        origin.forEach(person -> person.setProperties2(p2));
        origin.forEach(person -> person.setName(null));

        Workbook workbook = mapper.writeValueAsBook(origin, Person.class);
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            System.out.println(sheet.getSheetName());
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING: {
                            System.out.print(cell.getStringCellValue());
                            break;
                        }
                        case NUMERIC: {
                            System.out.print(cell.getNumericCellValue());
                            break;
                        }
                        case BOOLEAN: {
                            System.out.print(cell.getBooleanCellValue());
                            break;
                        }
                    }
                    System.out.print(" | ");
                }
                System.out.print(" | " + System.lineSeparator());
            }
        }

        List<Person> people = mapper.readValues(workbook, Person.class);
        for (int i = 0; i < 10; i++) {
            System.out.println(people.get(i));
            System.out.println(origin.get(i).equals(people.get(i)));
        }
    }

    @Test
    @SneakyThrows
    public void date() {
        Dates dates = new Dates();
        Date date = new Date();
        Instant instant = Instant.ofEpochMilli(date.getTime());
        instant.toEpochMilli();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDate localDate = localDateTime.toLocalDate();
        dates.setDate(date);
        dates.setInstant(instant);
        dates.setLocalDate(localDate);
        dates.setLocalDateTime(localDateTime);
        dates.setCalendar(Calendar.getInstance());
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        if (false) {
            javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ISO_DATE)); //yyyy-MM-dd
            javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ISO_DATE)); //yyyy-MM-dd

            javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            mapper.registerModule(new ExcelDateModule());
        }
        mapper.registerModule(javaTimeModule);

        List<Dates> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(dates);
        }
        mapper.writeValue(file, list, Dates.class);

        List<Dates> datesList = mapper.readValues(file, Dates.class);
        datesList.forEach(System.out::println);
    }

}
