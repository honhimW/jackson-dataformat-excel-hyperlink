# Jackson Format Excel-Hyperlink

The project is forked from [scndry/jackson-dataformat-spreadsheet](https://github.com/scndry/jackson-dataformat-spreadsheet), which can dump data objects as (.xlsx) format, with Hyperlink links between objects. Structured data types (Class) correspond to a (Sheet), and array and class array types are dumped in a Sheet named “List”. It also supports reading/writing.

---

### Maven
```xml
<project>
  <repositories>
    <repository>
      <id>sonatype-snapshots</id>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>

  <dependency>
    <groupId>io.github.honhimw</groupId>
    <artifactId>jackson-dataformat-excel-hyperlink</artifactId>
    <version>0.0.3-SNAPSHOT</version>
  </dependency>
</project>
```

### Gradle
```groovy
// Groovy
repositories {
    maven {
        name 'sonatype-snapshots'
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
    }
}

dependencies {
    implementation 'io.github.honhimw:jackson-dataformat-excel-hyperlink:0.0.3-SNAPSHOT'
}
```
```kotlin
// Kotlin
repositories {
    maven {
        name = "sonatype-snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("io.github.honhimw:jackson-dataformat-excel-hyperlink:0.0.3-SNAPSHOT")
}
```

---
## Excel-Hyperlink format

The commonly used (.xlsx) format document for office suite spreadsheets, which consists of three basic parts: sheet, row, and cell.

|                  | JSON       | JAVA                                | Excel-Hyperlink                                   |
|------------------|------------|-------------------------------------|---------------------------------------------------|
| object           | {}         | Type(.class)                        | sheet                                             |
| property         | key        | property                            | sheet title row/column index                      |
| array            | []         | array/Collection                    | sheet named "List" column index means array index |
| string           | ""         | String                              | string type cell value                            |
| numeric          | -1.1       | Number(int/short/long/float/double) | numeric type cell value                           |
| boolean          | true/false | Boolean                             | boolean type cell value                           |
| null             | null       | null                                | null/blank type cell value                        |
| separator        | ,          | memory                              | cell                                              |
| object reference | {}         | memory                              | Hyperlink                                         |
| map              | native     | Map.class interface                 | not supported💀                                   |

---
### Person
| id  | name  | address(Object) | properties |
|-----|-------|-----------------|------------|
| 1   | hello | #Address!2:2    | #List!1:1  |
| 2   | world | #Address!3:3    | #List!2:2  |

### Address
| country | postal_code |
|---------|-------------|
| HELLO   | 0000000     |
| WORLD   | 0000001     |


### List
| 0             | 1           | 2                           | 3                           | 4      | 5   |
|---------------|-------------|-----------------------------|-----------------------------|--------|-----|
| #Person!3:3   | hello       | 175.8                       | 121.3                       | master | |
| world         | hah         | #List!3:3                   |                             |        | |
| more and more | nested loop | Map types are not supported | column index is array index |        | |

---

## Usage

```java
public static void main(String[]args){
    HyperMapper mapper = new HyperMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    List<Person> people = List.of(); /* collection of pojo */
    File file = new File("some file");
    mapper.writeValue(file, people);

    List<person> readDatas = mapper.readValues(file, Person.class);
    Asserts.status(Objects.equals(people, readDatas), "supposed to be the same");
}
```

```java
import java.io.Serializable;

@JsonClassDescription("name for excel sheet")
@ThisIsACustomAnnotation("name for excel sheet") // supported by using TableNameResolver.class
public class Person implements Serializable {
    
    @JsonPropertyDescription("name for excel column")
    @ThisIsACustomAnnotation("name for excel column") // supported by using ColumnNameResolver.class
    private Long id;
    
    @JsonIgnore // jackson-annotations are supported
    private String remark;
}
```

### ReadVisitor/WriteVisitor
```java
HyperMapper mapper = new HyperMapper();
mapper.acceptWriteVisitor(visitor -> new BookWriteVisitor(visitor) {
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
mapper.acceptReadVisitor(visitor -> new BookReadVisitor(visitor) {
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
```
