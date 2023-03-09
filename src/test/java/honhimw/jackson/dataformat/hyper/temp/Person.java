package honhimw.jackson.dataformat.hyper.temp;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author hon_him
 * @since 2023-03-06
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {

    @JsonProperty(index = 0)
    private Long id;

    @JsonIgnore
    private String name;

    private String title;

    private Double height;

    private Boolean gender;

//    @JsonSerialize(converter = List2String.class)
//    @JsonDeserialize(converter = String2List.class)
    private List<String> properties;

    private List<Ext> properties2;

//    @JsonIgnore
    private Ext ext;
    private Ext ext2;
    private Ext ext3;

    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ext implements Serializable {
        @JsonProperty(index = 0)
        private String address;
        private String job;

        @JsonProperty(index = 1)
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
            return String.join("'", value);
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
