package org.getaviz.generator.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.getaviz.generator.rd.m2m.RD2RD;
import org.getaviz.generator.rd.s2m.JQA2RD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

public class RDBankTest {

	static DatabaseConnector connector;
	static Bank mockup = new Bank();

	@BeforeAll
	static void setup() {
		mockup.setupDatabase("./test/databases/RDBankTest.db");
		mockup.loadProperties("RDBankTest.properties");
		connector = mockup.getConnector();

		new JQA2RD();
		new RD2RD();
	}

	@AfterAll
	static void close() {
		mockup.close();
	}

	@Test
	void numberOfVisualizedPackages() {
		Record result = connector
				.executeRead("MATCH (disk:Disk)-[:VISUALIZES]->(:Package) RETURN count(disk) AS result").single();
		int numberOfVisualizedPackages = result.get("result").asInt();
		assertEquals(3, numberOfVisualizedPackages);
	}

	@Test
	void numberOfVisualizedTypes() {
		Record result = connector.executeRead("MATCH (disk:Disk)-[:VISUALIZES]->(:Type) RETURN count(disk) AS result")
				.single();
		int numberOfVisualizedTypes = result.get("result").asInt();
		assertEquals(4, numberOfVisualizedTypes);
	}

	@Test
	void numberOfVisualizedMethods() {
		Record result = connector
				.executeRead("MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Method) RETURN count(segment) AS result")
				.single();
		int numberOfVisualizedMethods = result.get("result").asInt();
		assertEquals(2, numberOfVisualizedMethods);
	}

	@Test
	void layoutAlgorithmPackage() {
		String hash = "ID_4481fcdc97864a546f67c76536e0308a3058f75d";
		Record result = connector.executeRead(
				"MATCH (:Package {hash: '" + hash + "'})<-[:VISUALIZES]-(:Disk)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z")
				.single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(0, x);
		assertEquals(-6, y);
		assertEquals(0, z);
	}

	@Test
	void layoutAlgorithmClass() {
		String hash = "ID_26f25e4da4c82dc2370f3bde0201e612dd88c04c";
		Record result = connector
				.executeRead("MATCH (:Type {hash: '" + hash + "'})<-[:VISUALIZES]-(:Disk)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z")
				.single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(0, x);
		assertEquals(-8, y);
		assertEquals(0, z);
	}
	
	@Test
	void layoutAlgorithmClass2() {
		String hash = "ID_bdd240c8fe7174e6ac1cfdd5282de76eb7ad6815";
		Record result = connector.executeRead(
				"MATCH (:Package {hash: '" + hash + "'})<-[:VISUALIZES]-(:Disk)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z")
				.single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(0, x);
		assertEquals(0, y);
		assertEquals(0, z);
	}

	@Test
	void methodSorting() {
		String hash = "ID_d2955e64b6776b754f9d69f8f480a62f849584ca";
		Record result = connector.executeRead("MATCH (segment:DiskSegment)-[:VISUALIZES]->(:Method {hash: '" + hash
				+ "'}) RETURN segment.anglePosition as anglePosition").single();
		double anglePosition = result.get("anglePosition").asDouble();
		assertEquals(180.00552486187846, anglePosition);
	}
	
	@Test
	void methodSorting1() {
		String hash = "ID_052028080c3d64381f56397d59113102bf4e5921";
		Record result = connector.executeRead("MATCH (:Package {hash: '" + hash + "'})<-[:VISUALIZES]-(:Disk)-[:HAS]->(position:Position) "
						+ "RETURN position.x as x, position.y as y, position.z as z").single();
		// double anglePosition = result.get("anglePosition").asDouble();
		// assertEquals(180.00552486187846, anglePosition);
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(0, x);
		assertEquals(11.046265, y);
		assertEquals(0, z);
	}

	@Test
	void layoutAlgorithmPackage1() {
		String hash = "ID_9fa088272bab165867bfaddcfbf58d6a7d5d45a2";
		Record result = connector.executeRead("MATCH (:Method {hash: '" + hash	+ "'})<-[:VISUALIZES]-(segment:DiskSegment)-[:HAS]->(position:Position)"
					 + "RETURN position.x as x, position.y as y, position.z as z").single();
		double x = result.get("x").asDouble();
		double y = result.get("y").asDouble();
		double z = result.get("z").asDouble();
		assertEquals(0, x);
		assertEquals(1.046265, y);
		assertEquals(0, z);
	}
}
