from neo4j import GraphDatabase

uri = "bolt://localhost:7687"
username = "neo4j"
password = "local-dev-password" # Replace with your actual password

try:
    driver = GraphDatabase.driver(uri, auth=(username, password))
    with driver.session() as session:
        result = session.run("CALL db.info()")
        print("Successfully connected to Neo4j.")
        print(f"Neo4j Version: {result.single()['name']}") # Example for getting database name
    driver.close()
except Exception as e:
    print(f"Failed to connect to Neo4j: {e}")