package org.getaviz.generator.city.m2t;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.Labels;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.io.FileWriter;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.types.Node;
import java.util.ArrayList;
import java.util.List;

import org.getaviz.generator.OutputFormatHelper;

public class City2AFrame {
	private SettingsConfiguration config = SettingsConfiguration.getInstance();
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());

	public City2AFrame() {
		log.info("City2AFrame has started");
		FileWriter fw = null;
		String fileName = "model.html";

		try {
			fw = new FileWriter(config.getOutputPath() + fileName);
			fw.write(OutputFormatHelper.AFrameHead() + toAFrameModel() + OutputFormatHelper.AFrameTail());
		} catch (IOException e) {
			log.error("Could not create file");
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		log.info("City2AFrame has finished");
	}

	private String toAFrameModel() {
		StringBuilder districts = new StringBuilder();
		StringBuilder buildings = new StringBuilder();
		StringBuilder segments = new StringBuilder();
		connector.executeRead(
				"MATCH (n:Model)-[:CONTAINS*]->(d:District)-[:HAS]->(p:Position) WHERE n.building_type = \'"
						+ config.getBuildingTypeAsString() + "\' RETURN d,p")
				.forEachRemaining((record) -> {
					districts.append(toDistrict(record.get("d").asNode(), record.get("p").asNode()));
				});
		if (config.getBuildingType() == BuildingType.CITY_ORIGINAL || config.isShowBuildingBase()) {
			connector.executeRead(
					"MATCH (n:Model)-[:CONTAINS*]->(b:Building)-[:HAS]->(p:Position) WHERE n.building_type = \'"
							+ config.getBuildingTypeAsString() + "\' RETURN b,p")
					.forEachRemaining((record) -> {
						buildings.append(toBuilding(record.get("b").asNode(), record.get("p").asNode()));
					});
		}

		if (!(config.getBuildingType() == BuildingType.CITY_ORIGINAL)) {
			connector.executeRead(
					"MATCH (n:Model)-[:CONTAINS*]->(bs:BuildingSegment)-[:HAS]->(p:Position) WHERE n.building_type = \'"
							+ config.getBuildingTypeAsString() + "\' RETURN bs,p")
					.forEachRemaining((record) -> {
						Node segment = record.get("bs").asNode();
						if (segment.hasLabel(Labels.Floor.name())) {
							segments.append(toFloor(segment, record.get("p").asNode()));
						} else if (segment.hasLabel(Labels.Chimney.name())) {
							segments.append(toChimney(segment, record.get("p").asNode()));
						} else {
							segments.append(toBuildingSegment(segment, record.get("p").asNode()));
						}
					});
		}
		return districts.toString() + buildings + segments;
	}

	private String toDistrict(Node district, Node position) {
		Node entity = connector.getVisualizedEntity(district.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<a-entity geometry=\"primitive:box; skipCache: true; buffer:true;" + " width:" + district.get("width") + ";" + " height:" +  district.get("height") + ";" + " depth:" +  district.get("length") + "\"");
		builder.append("\n");
		builder.append("id=\"" + entity.get("hash").asString() + "\"");
		builder.append("\n");
		builder.append("\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"");
		builder.append("\n");
		builder.append("\t shader=\"flat\"");
		builder.append("\n");
		builder.append("\t flat-shading=\"true\"");
		builder.append("\n");
		builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + district.get("color").asString() + "\"" + ">");
		builder.append("\n");
		builder.append("</a-entity>");
		builder.append("\n");
		return builder.toString();
	}
	
	private String toBuilding(Node building, Node position) {
		Node entity = connector.getVisualizedEntity(building.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<a-entity geometry=\"primitive:box; skipCache: true; buffer:true;" + " width:" + building.get("width") + ";" + " height:" +  building.get("height") + ";" + " depth:" +  building.get("length") + "\"");
		builder.append("\n");
		builder.append("id=\"" + entity.get("hash").asString() + "\"");
		builder.append(
				"\t\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"");
		builder.append("\n");
		builder.append("\t shader=\"flat\"");
		builder.append("\n");
		builder.append("\t flat-shading=\"true\"");
		builder.append("\n");			
		builder.append("\t\t vertex-colors-buffer=\"" + "baseColor:" + building.get("color").asString() + "\"" + ">");
		builder.append("\n");
		builder.append("</a-entity>");
		builder.append("\n");
		return builder.toString();
	}
	
	private String buildPosition(Node position) {
		return "\t position=\"" + position.get("x") + " " + position.get("y") + " " + position.get("z") + "\"";
	}
	
	private String buildColor(Node segment) {
		return "\t vertex-colors-buffer=\"" + "baseColor:" + segment.get("color").asString() + "\"";
	}
	
	private String toBuildingSegment(Node segment, Node position) {
		Node entity = connector.getVisualizedEntity(segment.id());
		List<Node> separators = new ArrayList<>();
		connector.executeRead("MATCH (n)-[:HAS]->(ps:PanelSeparator) RETURN ps").forEachRemaining((record) -> {
			separators.add(record.get("ps").asNode());
		});
		double width = segment.get("width").asDouble();
		double height = segment.get("height").asDouble();
		double length = segment.get("length").asDouble();
		StringBuilder builder = new StringBuilder();
		if (config.getBuildingType() == BuildingType.CITY_PANELS && entity.hasLabel(Labels.Field.name())
				&& config.isShowAttributesAsCylinders()) {
			builder.append("<a-entity geometry=\"primitive:cylinder; skipCache: true; buffer:true;" + " radius:" + width/2 + ";" + " height:" +  "\"");
			builder.append("\n");
			builder.append("id=\"" + entity.get("hash").asString() + "\"");
			builder.append("\n");
			builder.append(buildPosition(position));
			builder.append("\n");
			builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + segment.get("color").asString() + "\"");
			builder.append("\n");
			builder.append("\t shader=\"flat\"");
			builder.append("\n");
			builder.append("\t flat-shading=\"true\"");
			builder.append("\n");
			builder.append("\t segments-height=\"2\"");
			builder.append("\n");
			builder.append("\t segments-radial=\"20\">" );
			builder.append("\n");
			builder.append("</a-entity>");
			builder.append("\n");
		} else {
			builder.append("<a-entity geometry=\"primitive:box; skipCache: true; buffer:true;" + " width:" + width + ";" + " height:" +  height + ";" + " depth:" +  length + "\"");
			builder.append("\n");
			builder.append("id=\"" + entity.get("hash").asString() + "\"");
			builder.append("\n");
			builder.append(buildPosition(position));
			builder.append("\n");
			builder.append("\t shader=\"flat\"");
			builder.append("\n");
			builder.append("\t flat-shading=\"true\"");
			builder.append("\n");
			builder.append(buildColor(segment)  + ">");
			builder.append("\n");
			builder.append("</a-entity>");
			builder.append("\n");
		}
		for (final Node separator : separators) {
			final Node pos = connector.getPosition(separator.id());
			builder.append("\n");
			if (separator.hasLabel(Labels.Cylinder.name())) {
			builder.append("<a-entity geometry=\"primitive:cylinder; skipCache: true; buffer:true;" + " radius:" + separator.get("radius") + ";" + " height:" +config.getPanelSeparatorHeight() + "\"");
			builder.append("\n");
			builder.append("id=\"" + entity.get("hash").asString() + "\"");
			builder.append("\n");
			builder.append(buildPosition(pos));
			builder.append("\n");
			builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + config.getCityColorHex("black") + "\"");
			builder.append("\n");
			builder.append("\t shader=\"flat\"");
			builder.append("\n");
			builder.append("\t flat-shading=\"true\"");
			builder.append("\n");
			builder.append("\t segments-height=\"2\"");
			builder.append("\n");
			builder.append("\t segments-radial=\"20\">");
			builder.append("\n");
			builder.append("</a-entity>");
			builder.append("\n");
			} else {
			builder.append("<a-entity geometry=\"primitive:box; skipCache: true; buffer:true;" + " width:" + separator.get("width") + ";" + " height:" +  config.getPanelSeparatorHeight() + ";" + " depth:" +  separator.get("length") + "\"");
			builder.append("\n");
			builder.append("id=\"" + entity.get("hash").asString() + "\"");
			builder.append("\n");
			builder.append(buildPosition(pos));
			builder.append("\n");
			builder.append("\t shader=\"flat\"");
			builder.append("\n");
			builder.append("\t flat-shading=\"true\"");
			builder.append("\n");
			builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + config.getCityColorHex("black") + "\"" + ">");
			builder.append("\n");
			builder.append("</a-entity>");
			builder.append("\n");
			}
		}
		return builder.toString();
	}
	
	private String toFloor(Node floor, Node position) {
		Node entity = connector.getVisualizedEntity(floor.id());
		StringBuilder builder = new StringBuilder();
		builder.append("<a-entity geometry=\"primitive:box; skipCache: true; buffer:true;" + " width:" + floor.get("width") + ";" + " height:" +  floor.get("height") + ";" + " depth:" +  floor.get("length") + "\"");
		builder.append("\n");
		builder.append("id=\"" + entity.get("hash").asString() + "\"");
		builder.append("\n");
		builder.append(buildPosition(position));
		builder.append("\n");
		builder.append("\t shader=\"flat\"");
		builder.append("\n");
		builder.append("\t flat-shading=\"true\"");
		builder.append("\n");
		builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + floor.get("color").asString() + "\"" + ">");
		builder.append("\n");
		builder.append("</a-entity>");
		builder.append("\n");
		return builder.toString();
	}
	
	private String toChimney(Node chimney, Node position) {
		Node entity = connector.getVisualizedEntity(chimney.id());		
		StringBuilder builder = new StringBuilder();
		builder.append("<a-entity geometry=\"primitive:box; skipCache: true; buffer:true;" + " width:" + chimney.get("width") + ";" + " height:" +  chimney.get("height") + ";" + " depth:" +  chimney.get("length") + "\"");
		builder.append("\n");
	    builder.append("id=\"" + entity.get("hash").asString() + "\"");
	    builder.append("\n");
	    builder.append(buildPosition(position));
	    builder.append("\n");
	    builder.append("\t shader=\"flat\"");
		builder.append("\n");
		builder.append("\t flat-shading=\"true\"");
		builder.append("\n");
		builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + chimney.get("color").asString() + "\"" + ">");
	    builder.append("\n");
	    builder.append("</a-entity>");
	    builder.append("\n");
	    return builder.toString();
	}
}
