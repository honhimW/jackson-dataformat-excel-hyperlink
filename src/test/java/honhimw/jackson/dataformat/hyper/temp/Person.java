package honhimw.jackson.dataformat.hyper.temp;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.ListUtils;

/**
 * @author hon_him
 * @since 2023-03-06
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("人")
public class Person implements Serializable {

    @JsonProperty(index = 0)
    private Long id;

    @JsonIgnore
    private String name;

    private String title;

    @JsonProperty(index = 2)
    @JsonPropertyDescription("身高")
    private Double height;

    private Boolean gender;

//    @JsonSerialize(converter = List2String.class)
//    @JsonDeserialize(converter = String2List.class)
    private List<String> properties;

//    @JsonIgnore
    private List<Object> properties2;

//    @JsonIgnore
    @JsonProperty(index = 3)
    private Ext ext;
    private Ext ext2;
    private Ext ext3;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(name, person.name)
            && Objects.equals(title, person.title) && Objects.equals(height, person.height)
            && Objects.equals(gender, person.gender) && Objects.equals(ext, person.ext)
            && Objects.equals(ext2, person.ext2) && Objects.equals(ext3, person.ext3)
            && ListUtils.isEqualList(properties, ((Person) o).properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, title, height, gender, properties, properties2, ext, ext2, ext3);
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ext implements Serializable {
        @JsonProperty(index = 0)
        private String address;
        @JsonProperty(index = 3)
        @JsonPropertyDescription("工作")
        private Double job;

        @JsonProperty(index = 2)
        private More more;

        @Data
        @EqualsAndHashCode(callSuper = false)
        @NoArgsConstructor
        @AllArgsConstructor
        public static class More implements Serializable {
            private String info;
            private Boolean enable;
        }

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @JsonPropertyDescription("身高")
    public Double getHeight() {
        return height;
    }

    public Boolean getGender() {
        return gender;
    }

    public List<String> getProperties() {
        return properties;
    }

    public Ext getExt() {
        return ext;
    }

    public Ext getExt2() {
        return ext2;
    }

    public static class List2String implements Converter<List<String>, String> {

        @Override
        public String convert(final List<String> value) {
            return String.join(";", value);
        }

        @Override
        public JavaType getInputType(final TypeFactory typeFactory) {
            return typeFactory.constructCollectionType(ArrayList.class, String.class);
        }

        @Override
        public JavaType getOutputType(final TypeFactory typeFactory) {
            return typeFactory.constructType(String.class);
        }
    }

    public static class String2List implements Converter<String, List<String>> {

        @Override
        public List<String> convert(final String value) {
            return Arrays.stream(value.split(";")).toList();
        }

        @Override
        public JavaType getInputType(final TypeFactory typeFactory) {
            return typeFactory.constructType(String.class);
        }

        @Override
        public JavaType getOutputType(final TypeFactory typeFactory) {
            return typeFactory.constructCollectionType(ArrayList.class, String.class);
        }
    }


}
