package honhimw.jackson.dataformat.hyper.temp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import honhimw.jackson.dataformat.hyper.ExcelDateModule;
import honhimw.jackson.dataformat.hyper.HyperMapper;
import honhimw.jackson.dataformat.hyper.deser.CellValue;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowWriteVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetWriteVisitor;
import honhimw.jackson.dataformat.hyper.temp.Person.Ext;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.SneakyThrows;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * @author hon_him
 * @since 2023-03-06
 */

public class Tests {

    @Test
    @SneakyThrows
    public void excelMapper() {
        List<Person> origin = new ArrayList<>(MockUtils.generate(Person.class, 10));
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        origin.forEach(person -> person.setProperties(List.of("hello", "world")));
        List<Object> p2 = new ArrayList<>(MockUtils.generate(Ext.class,
            ThreadLocalRandom.current().nextInt(2,5)));
        p2.add("hello");
        p2.add(123);
        p2.add(List.of("a", "b"));
        origin.forEach(person -> person.setProperties2(p2));
        origin.forEach(person -> person.setName(null));
        File file = new File("E:\\temp\\1234.xlsx");
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
        mapper.acceptWriteVisitor(new BookWriteVisitor() {
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
        mapper.acceptReadVisitor(new BookReadVisitor() {
            @Override
            public SheetReadVisitor visitSheet(final Sheet sheet) {
                SheetReadVisitor sheetReadVisitor = super.visitSheet(sheet);
                System.out.println("read sheet: " + sheet.getSheetName());

                return new SheetReadVisitor(sheetReadVisitor) {
                    @Override
                    public RowReadVisitor visitRow(final Row row) {
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
        });
        File file = new File("E:\\temp\\person.xlsx");
        mapper.writeValue(file, List.of(Person.VALUE), Person.class);
        Person person = mapper.readValue(file, Person.class);
        System.out.println(person.toString());
    }

    @Test
    @SneakyThrows
    public void inMemory() {
        List<Person> origin = new ArrayList<>(MockUtils.generate(Person.class, 10));
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        origin.forEach(person -> person.setProperties(List.of("hello", "world")));
        List<Object> p2 = new ArrayList<>(MockUtils.generate(Ext.class,
            ThreadLocalRandom.current().nextInt(2,5)));
        p2.add("hello");
        p2.add(123);
        p2.add(List.of("a", "b"));
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
                        case STRING -> System.out.print(cell.getStringCellValue());
                        case NUMERIC -> System.out.print(cell.getNumericCellValue());
                        case BOOLEAN -> System.out.print(cell.getBooleanCellValue());
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
        LocalDate localDate = LocalDate.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
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

        File file = new File("E:\\temp\\2345.xlsx");
        List<Dates> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(dates);
        }
        mapper.writeValue(file, list, Dates.class);

        List<Dates> datesList = mapper.readValues(file, Dates.class);
        datesList.forEach(System.out::println);
    }

}
