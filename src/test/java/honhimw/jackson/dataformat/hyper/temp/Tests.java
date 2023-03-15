package honhimw.jackson.dataformat.hyper.temp;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import honhimw.jackson.dataformat.hyper.HyperMapper;
import honhimw.jackson.dataformat.hyper.temp.Person.Ext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.SneakyThrows;
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
        Collection<Person> generate = MockUtils.generate(Person.class, 10);
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        generate.forEach(person -> person.setProperties(List.of("hello", "world")));
        List<Object> p2 = new ArrayList<>(MockUtils.generate(Ext.class,
            ThreadLocalRandom.current().nextInt(2,5)));
        p2.add("hello");
        p2.add(123);
        p2.add(List.of("a", "b"));
        generate.forEach(person -> person.setProperties2(p2));
        generate.forEach(person -> person.setName(null));
        File file = new File("E:\\temp\\1234.xlsx");
        mapper.writeValue(file, generate, Person.class);

        List<Person> people = mapper.readValues(file, Person.class);
        List<Person> origin = new ArrayList<>(generate);
        for (int i = 0; i < 10; i++) {
            System.out.println(people.get(i));
            System.out.println(origin.get(i).equals(people.get(i)));
        }
    }

    @Test
    @SneakyThrows
    public void inMemory() {
        System.out.println(Person.class.getAnnotation(JsonClassDescription.class).value());
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

}
