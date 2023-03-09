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

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import honhimw.jackson.dataformat.hyper.deser.SheetInput;
import honhimw.jackson.dataformat.hyper.deser.SheetParser;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class HyperReader extends ObjectReader {

    HyperReader(final SpreadsheetMapper mapper, final DeserializationConfig config) {
        super(mapper, config);
    }

    HyperReader(final SpreadsheetMapper mapper, final DeserializationConfig config, final JavaType valueType,
                      final Object valueToUpdate, final FormatSchema schema,
                      final InjectableValues injectableValues) {
        super(mapper, config, valueType, valueToUpdate, schema, injectableValues);
    }

    @SuppressWarnings("java:S107")
    private HyperReader(final HyperReader base, final DeserializationConfig config, final JavaType valueType,
                              final JsonDeserializer<Object> rootDeser, final Object valueToUpdate,
                              final FormatSchema schema, final InjectableValues injectableValues,
                              final DataFormatReaders dataFormatReaders) {
        super(base, config, valueType, rootDeser, valueToUpdate, schema, injectableValues, dataFormatReaders);
    }

    private HyperReader(final HyperReader base, final DeserializationConfig config) {
        super(base, config);
    }

    private HyperReader(final HyperReader base, final HyperFactory f) {
        super(base, f);
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************
    /* Helper methods used internally for invoking constructors
    /**********************************************************
     */

    @Override
    protected HyperReader _new(final ObjectReader base, final JsonFactory f) {
        return new HyperReader((HyperReader) base, (HyperFactory) f);
    }

    @Override
    protected HyperReader _new(final ObjectReader base, final DeserializationConfig config) {
        return new HyperReader((HyperReader) base, config);
    }

    @Override
    protected HyperReader _new(final ObjectReader base, final DeserializationConfig config, final JavaType valueType,
                                     final JsonDeserializer<Object> rootDeser, final Object valueToUpdate,
                                     final FormatSchema schema, final InjectableValues injectableValues,
                                     final DataFormatReaders dataFormatReaders) {
        return new HyperReader((HyperReader) base, config, valueType,
                rootDeser, valueToUpdate, schema, injectableValues, dataFormatReaders);
    }

    @Override
    protected <T> SheetMappingIterator<T> _newIterator(final JsonParser p, final DeserializationContext ctxt,
                                                       final JsonDeserializer<?> deser, final boolean parserManaged) {
        return new SheetMappingIterator<>(_valueType, p, ctxt, deser, parserManaged, _valueToUpdate);
    }

    /*
    /**********************************************************
    /* Life-cycle, fluent factory methods, other
    /**********************************************************
     */

    @Override
    public HyperReader forType(final Class<?> valueType) {
        return (HyperReader) super.forType(valueType);
    }

    /*
    /**********************************************************
    /* Factory methods for creating SheetParsers
    /**********************************************************
     */

    public SheetParser createParser(final Sheet src) {
        return (SheetParser) _config.initialize(parserFactory().createParser(src), _schema);
    }

    public SheetParser createParser(final SheetInput<?> src) throws IOException {
        return (SheetParser) _config.initialize(parserFactory().createParser(src), _schema);
    }

    /*
    /**********************************************************
    /* Deserialization methods; others
    /**********************************************************
     */

    public <T> T readValue(final Sheet src) throws IOException {
        try (MappingIterator<T> iterator = readValues(src)) {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }

    public <T> T readValue(final SheetInput<?> src) throws IOException {
        try (MappingIterator<T> iterator = readValues(src)) {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }

    @Override
    public <T> T readValue(final File src) throws IOException {
        try (MappingIterator<T> iterator = readValues(src)) {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }

    @Override
    public <T> T readValue(final InputStream src) throws IOException {
        try (MappingIterator<T> iterator = readValues(src)) {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }

    /*
    /**********************************************************
    /* Deserialization methods; reading sequence of values
    /**********************************************************
     */

    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    public <T> SheetMappingIterator<T> readValues(final Sheet src) throws IOException {
        return (SheetMappingIterator<T>) _bindAndReadValues(_considerFilter(createParser(src), true));
    }

    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    public <T> SheetMappingIterator<T> readValues(final SheetInput<?> src) throws IOException {
        return (SheetMappingIterator<T>) _bindAndReadValues(_considerFilter(createParser(src), true));
    }

    @Override
    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    public <T> SheetMappingIterator<T> readValues(final File src) throws IOException {
        return (SheetMappingIterator<T>) super.readValues(src);
    }

    @Override
    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    public <T> SheetMappingIterator<T> readValues(final InputStream src) throws IOException {
        return (SheetMappingIterator<T>) super.readValues(src);
    }

    /*
    /**********************************************************
    /* Other public methods
    /**********************************************************
     */

    public HyperFactory parserFactory() {
        return (HyperFactory) _parserFactory;
    }
}