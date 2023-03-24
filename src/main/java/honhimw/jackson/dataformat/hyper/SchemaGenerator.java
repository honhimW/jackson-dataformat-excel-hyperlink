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

package honhimw.jackson.dataformat.hyper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.generator.ColumnNameResolver;
import honhimw.jackson.dataformat.hyper.schema.generator.FormatVisitorWrapper;
import honhimw.jackson.dataformat.hyper.schema.generator.TableNameResolver;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellAddress;

@Slf4j
public final class SchemaGenerator {

    private final GeneratorSettings _generatorSettings;

    public SchemaGenerator() {
        _generatorSettings = new GeneratorSettings(
            CellAddress.A1,
            ColumnNameResolver.DEFAULT,
            TableNameResolver.DEFAULT,
            bookWriteVisitor -> bookWriteVisitor,
            bookReadVisitor -> bookReadVisitor
        );
    }

    private SchemaGenerator(final GeneratorSettings generatorSettings) {
        _generatorSettings = generatorSettings;
    }

    public SchemaGenerator withOrigin(final CellAddress origin) {
        return new SchemaGenerator(_generatorSettings.with(origin));
    }

    public SchemaGenerator withColumnNameResolver(final ColumnNameResolver resolver) {
        return new SchemaGenerator(_generatorSettings.with(resolver));
    }

    public SchemaGenerator withTableNameResolver(final TableNameResolver resolver) {
        return new SchemaGenerator(_generatorSettings.with(resolver));
    }

    public SchemaGenerator withBookWriteVisitor(final Function<BookWriteVisitor, BookWriteVisitor> visitorBuilder) {
        return new SchemaGenerator(_generatorSettings.withWriteVisitor(visitorBuilder));
    }

    public SchemaGenerator withBookReadVisitor(final Function<BookReadVisitor, BookReadVisitor> visitorBuilder) {
        return new SchemaGenerator(_generatorSettings.withReadVisitor(visitorBuilder));
    }

    HyperSchema generate(final JavaType type, final DefaultSerializerProvider provider,
        final SerializerFactory factory)
        throws JsonMappingException {
        _verifyType(type, provider);
        final FormatVisitorWrapper visitor = new FormatVisitorWrapper();
        final SerializationConfig config = provider.getConfig()
            .withAttribute(ColumnNameResolver.class, _generatorSettings._columnNameResolver)
            .withAttribute(TableNameResolver.class, _generatorSettings._tableNameResolver);
        final DefaultSerializerProvider instance = provider.createInstance(config, factory);
        try {
            instance.acceptJsonFormatVisitor(type, visitor);
        } catch (Exception e) {
            throw _invalidSchemaDefinition(type, e);
        }
        final List<Column> columns = new ArrayList<>();
        for (final Column column : visitor) {
            columns.add(column);
            if (log.isTraceEnabled()) {
                log.trace(column.toString());
            }
        }
        return new HyperSchema(columns, visitor.getTables(), _generatorSettings._origin,
            _generatorSettings._bookWriteVisitor, _generatorSettings._bookReadVisitor);
    }

    private void _verifyType(final JavaType type, final DefaultSerializerProvider provider)
        throws JsonMappingException {
        if (type.isArrayType() || type.isCollectionLikeType()) {
            throw _invalidSchemaDefinition(type, "can NOT be a Collection or array type");
        }
    }

    private JsonMappingException _invalidSchemaDefinition(final JavaType type, final String message) {
        return _invalidSchemaDefinition(type, "Root type of a schema " + message, null);
    }

    private JsonMappingException _invalidSchemaDefinition(final JavaType type, final Throwable cause) {
        return _invalidSchemaDefinition(type, cause.getMessage(), cause);
    }

    private JsonMappingException _invalidSchemaDefinition(final JavaType type, final String problem,
        final Throwable cause) {
        final String msg = String.format("Failed to generate schema of type '%s' for %s, problem: %s",
            HyperSchema.SCHEMA_TYPE,
            ClassUtil.getTypeDescription(type), problem);
        return InvalidDefinitionException.from((JsonGenerator) null, msg, type).withCause(cause);
    }

    @AllArgsConstructor
    static final class GeneratorSettings {

        private CellAddress _origin;
        private ColumnNameResolver _columnNameResolver;
        private TableNameResolver _tableNameResolver;
        private Function<BookWriteVisitor, BookWriteVisitor> _bookWriteVisitor;
        private Function<BookReadVisitor, BookReadVisitor> _bookReadVisitor;

        private GeneratorSettings with(final CellAddress origin) {
            GeneratorSettings copied = copy();
            copied._origin = origin;
            return copied;
        }

        private GeneratorSettings with(final ColumnNameResolver resolver) {
            GeneratorSettings copied = copy();
            copied._columnNameResolver = resolver;
            return copied;
        }

        private GeneratorSettings with(final TableNameResolver resolver) {
            GeneratorSettings copied = copy();
            copied._tableNameResolver = resolver;
            return copied;
        }

        private GeneratorSettings withWriteVisitor(final Function<BookWriteVisitor, BookWriteVisitor> visitorBuilder) {
            GeneratorSettings copied = copy();
            copied._bookWriteVisitor = visitorBuilder;
            return copied;
        }

        private GeneratorSettings withReadVisitor(final Function<BookReadVisitor, BookReadVisitor> visitorBuilder) {
            GeneratorSettings copied = copy();
            copied._bookReadVisitor = visitorBuilder;
            return copied;
        }

        public GeneratorSettings copy() {
            return new GeneratorSettings(_origin, _columnNameResolver, _tableNameResolver, _bookWriteVisitor, _bookReadVisitor);
        }


    }
}
