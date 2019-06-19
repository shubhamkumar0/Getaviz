package org.getaviz.generator.rd.m2t;

import org.getaviz.generator.SettingsConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.OutputFormatHelper;
import java.io.FileWriter;
import java.io.IOException;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.database.DatabaseConnector;
import java.util.ArrayList;
import java.util.List;

public class RD2AFrame {
	private SettingsConfiguration config = SettingsConfiguration.getInstance();
	private DatabaseConnector connector = DatabaseConnector.getInstance();
	private Log log = LogFactory.getLog(this.getClass());

	public RD2AFrame() {
		log.info("RD2AFrame has started");
		FileWriter fw = null;
		String fileName = "model.html";
		try {
			fw = new FileWriter(config.getOutputPath() + fileName);
			fw.write(OutputFormatHelper.AFrameHead() + toX3DOMRD() + OutputFormatHelper.AFrameTail());
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
		log.info("RD2AFrame has finished");
	}

	private String toX3DOMRD() {
		StringBuilder elements = new StringBuilder();
		connector.executeRead(
				"MATCH (element)<-[:VISUALIZES]-(d:Disk)-[:HAS]->(p:Position) RETURN d,p, element.hash ORDER BY element.hash")
				.forEachRemaining((result) -> {
					elements.append(toDisk(result.get("d").asNode(), result.get("p").asNode()));
				});
		return elements.toString();
	}

	private String toDisk(Node disk, Node position) {
		double radius = disk.get("radius").asDouble();
		Node entity = connector.getVisualizedEntity(disk.id());
		ArrayList<Node> segments = new ArrayList<>();
		connector.executeRead("MATCH (n)-[:CONTAINS]->(ds:DiskSegment)-[:VISUALIZES]->(element) WHERE ID(n) = "
				+ disk.id() + " RETURN ds, element.hash ORDER BY element.hash").forEachRemaining((result) -> {
					segments.add(result.get("ds").asNode());
				});
		StringBuilder builder = new StringBuilder();
		if (radius - config.getRDRingWidth() == 0) {
			builder.append("<a-entity geometry=\"primitive:circle; skipCache: true; buffer:true;" + " radius:" + radius + "\"");
			builder.append("\n");
			builder.append("id=\"" + entity.get("hash").asString() + "\" ");
			builder.append("\n");
			builder.append("\t position=\"" + position.get("x") + " ");
			builder.append(position.get("y") + " ");
			builder.append(position.get("z") + "\"");
			builder.append("\n");
			builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + disk.get("color").asString() + "\"");
			builder.append("\n");
			builder.append("\t shader=\"flat\"");
			builder.append("\n");
			builder.append("\t flat-shading=\"true\"");
			builder.append("\n");
			builder.append("\t depth-test=\"false\"");
			builder.append("\n");
			builder.append("\t depth-write=\"false\">");
			builder.append("\n");
			builder.append("\t" + toSegment(segments));
			builder.append("\n");
			builder.append("</a-entity>");
			builder.append("\n");
		} else {
			builder.append("<a-entity geometry=\"primitive:ring; skipCache: true; buffer:true;" + " radius-inner:" + (radius - config.getRDRingWidth()) + ";"+ " radius-outer:" + radius + "\"");
			builder.append("\n");
			builder.append("id=\"" + entity.get("hash").asString() + "\"");
			builder.append("\n");
			builder.append("\t position=\"" + position.get("x") + " ");
			builder.append(position.get("y") + " ");
			builder.append(position.get("z") + "\"");
			builder.append("\n");
			builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + disk.get("color").asString() + "\"");
			builder.append("\n");
			builder.append("\t shader=\"flat\"");
			builder.append("\n");
			builder.append("\t flat-shading=\"true\"");
			builder.append("\n");
			builder.append("\t depth-test=\"false\"");
			builder.append("\n");
			builder.append("\t depth-write=\"false\"");
			builder.append("\n");
			builder.append("\t segments-phi=\"1\">");
			builder.append("\n");
			builder.append("\t" + toSegment(segments));
			builder.append("\n");
			builder.append("</a-entity>");
			builder.append("\n");
		}
		return builder.toString();
	}
	
	private String toSegment(List<Node> segments) {
		StringBuilder builder = new StringBuilder();
		for (final Node segment : segments) {
			Node entity = connector.getVisualizedEntity(segment.id());
			if (segment.get("innerRadius").asDouble() == 0) {
				builder.append("<a-entity geometry=\"primitive:circle; skipCache: true; buffer:true;" + " radius:" + segment.get("outerRadius") + "\"");
				builder.append("\n");
				builder.append("id=\"" + entity.get("hash").asString() + "\"");
				builder.append("\n");
				builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + segment.get("color").asString() + "\"");
				builder.append("\n");
				builder.append("\t theta-start=\"" + segment.get("anglePosition") + "\"");
				builder.append("\n");
				builder.append("\t theta-length=\"" + segment.get("angle") + "\"");
				builder.append("\n");
				builder.append("\t shader=\"flat\"");
				builder.append("\n");
				builder.append("\t flat-shading=\"true\"");
				builder.append("\n");
				builder.append("\t depth-test=\"false\"");
				builder.append("\n");
				builder.append("\t depth-write=\"false\">");
				builder.append("\n");
				builder.append("</a-entity>");
				builder.append("\n");
			} else {
				builder.append("<a-entity geometry=\"primitive:ring; skipCache: true; buffer:true;" + " radius-inner:" + segment.get("innerRadius") + ";" + " radius-outer:" + segment.get("outerRadius") +"\"");
				builder.append("\n");
				builder.append("id=\"" + entity.get("hash").asString() + "\"");
				builder.append("\n");
				builder.append("\t vertex-colors-buffer=\"" + "baseColor:" + segment.get("color").asString() + "\"");
				builder.append("\n");
				builder.append("\t shader=\"flat\"");
				builder.append("\n");
				builder.append("\t flat-shading=\"true\"");
				builder.append("\n");
				builder.append("\t depth-test=\"false\"");
				builder.append("\n");
				builder.append("\t depth-write=\"false\"");
				builder.append("\n");
				builder.append("\t theta-start=\"" + segment.get("anglePosition") + "\"");
				builder.append("\n");
				builder.append("\t theta-length=\"" + segment.get("angle") + "\"");
				builder.append("\n");
				builder.append("\t segments-phi=\"1\">");
				builder.append("\n");
				builder.append("</a-entity>");
				builder.append("\n");
			}
		}
		return builder.toString();
	}
}
