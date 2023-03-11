package honhimw.jackson.dataformat.hyper.temp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import honhimw.jackson.dataformat.hyper.HyperMapper;
import honhimw.jackson.dataformat.hyper.temp.Person.Ext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

/**
 * @author hon_him
 * @since 2023-03-06
 */

public class Tests {

    @Test
    @SneakyThrows
    public void excelMapper() {
        Collection<Person> generate = MockUtils.generate(Person.class, 20);
        HyperMapper mapper = new HyperMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        generate.forEach(person -> person.setProperties(List.of("hello", "world")));
        List<Object> p2 = new ArrayList<>(MockUtils.generate(Ext.class,
            ThreadLocalRandom.current().nextInt(2,5)));
        p2.add("hello");
        p2.add(123);
        generate.forEach(person -> person.setProperties2(p2));
        generate.forEach(person -> person.setName(null));
        File file = new File("E:\\temp\\1234.xlsx");
        mapper.writeValue(file, generate, Person.class);

//        List<Person> people = mapper.readValues(file, Person.class);
//        people.forEach(System.out::println);
    }

}
