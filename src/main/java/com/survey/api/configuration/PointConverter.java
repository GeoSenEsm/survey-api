package com.survey.api.configuration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;


@Converter(autoApply = true)
public class PointConverter implements AttributeConverter<Point, String>{
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKTReader reader = new WKTReader(geometryFactory);
    private final WKTWriter writer = new WKTWriter();

    @Override
    public String convertToDatabaseColumn(Point point) {
        return (point != null) ? writer.write(point) : null;
    }

    @Override
    public Point convertToEntityAttribute(String hexOrWkt) {
        try {
            String wkt;
            if (hexOrWkt != null && hexOrWkt.startsWith("0x")) {
               throw new IllegalArgumentException("Hexadecimal WKT detected, ensure SQL uses STAsText() for conversion.");
            } else {
                wkt = hexOrWkt;
            }
            return (wkt != null) ? (Point) reader.read(wkt) : null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert WKT to Point", e);
        }
    }
}
