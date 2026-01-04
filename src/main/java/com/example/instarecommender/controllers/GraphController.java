package com.example.instarecommender.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.instarecommender.models.AlgorithmTypes;
import com.example.instarecommender.models.InteractionDTO;
import com.example.instarecommender.models.InteractionType;
import com.example.instarecommender.services.DynamicWeightService;
import com.example.instarecommender.services.GraphService;
import com.example.instarecommender.services.RecommenderService;


@RestController
@RequestMapping("/api")
public class GraphController {

    private final DynamicWeightService dynamicWeightService;
    private final GraphService graphService;
    private final RecommenderService recommenderService;

    public GraphController(GraphService graphService, RecommenderService recommenderService, DynamicWeightService dynamicWeightService) {
        this.graphService = graphService;
        this.recommenderService = recommenderService;
        this.dynamicWeightService = dynamicWeightService;
    }

    @PostMapping("/interact")
    public ResponseEntity<String> interact(@RequestBody InteractionDTO payload) {
        String from = payload.getFrom();
        String to = payload.getTo();
        String typeStr = payload.getType().toString();

        if (from == null || to == null || typeStr == null) {
            return ResponseEntity.badRequest().body("Missing 'from', 'to' or 'type' fields");
        }

        try {
            InteractionType type = InteractionType.valueOf(typeStr.toUpperCase());
            dynamicWeightService.updateWeightBasedOnInteraction(from, to, type);
            return ResponseEntity.ok("Interaction recorded: " + type + " from " + from + " to " + to);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid interaction type: " + typeStr);
        }
    }

    @GetMapping("/recommend/{user}")
    public Object recommend(@PathVariable String user, @RequestParam AlgorithmTypes algorithm, @RequestParam(defaultValue = "10") int limit) {
        return recommenderService.recommend(user, algorithm, limit);
    }

    @GetMapping("/graph")
    public Object fullGraph() {
        return graphService.getGraph();
    }


    @PostMapping("/user")
    public ResponseEntity<String> addUser(@RequestParam String name) {
        graphService.addUser(name);
        return ResponseEntity.ok("User added: " + name);
    }

    @PostMapping("/follow")
    public ResponseEntity<String> addFollow(@RequestBody Map<String, String> payload) {
        String from = payload.get("from");
        String to = payload.get("to");
        double weight = Double.parseDouble(payload.get("weight"));

        graphService.addOrUpdateEdge(from, to, weight);

        return ResponseEntity.ok("Follow added: " + from + " -> " + to);
    }
    
    @GetMapping("/graph/visual")
    public String visualizeGraph() {
        return """
            <html><head><script src='https://d3js.org/d3.v7.min.js'></script>
            <style>
                body, html { margin: 0; padding: 0; height: 100%; overflow: hidden; }
                svg { width: 100%; height: 100%; }
            </style>
            </head><body>
            <svg></svg>
            <script>
            fetch('/api/graph').then(r => r.json()).then(data => {
                const nodes = {};
                const links = data.map(d => {
                    nodes[d.from] = {id: d.from};
                    nodes[d.to] = {id: d.to};
                    return { source: d.from, target: d.to, weight: d.weight };
                });
                const nodeArray = Object.values(nodes);

                const svg = d3.select("svg");
                const width = svg.node().getBoundingClientRect().width;
                const height = svg.node().getBoundingClientRect().height;

                svg.append("defs").append("marker")
                    .attr("id", "arrowhead")
                    .attr("viewBox", "-0 -5 10 10")
                    .attr("refX", 23)
                    .attr("refY", 0)
                    .attr("orient", "auto")
                    .attr("markerWidth", 10)
                    .attr("markerHeight", 10)
                    .append("path")
                    .attr("d", "M0,-5L10,0L0,5")
                    .attr("fill", "#999");

                const simulation = d3.forceSimulation(nodeArray)
                    .force("link", d3.forceLink(links).id(d => d.id).distance(150))
                    .force("charge", d3.forceManyBody().strength(-200))
                    .force("center", d3.forceCenter(width / 2, height / 2));

                const link = svg.append("g")
                    .selectAll("line")
                    .data(links)
                    .enter().append("line")
                    .attr("stroke", "#999")
                    .attr("stroke-opacity", 0.6)
                    .attr("stroke-width", d => Math.sqrt(d.weight))
                    .attr("marker-end", "url(#arrowhead)");

                const node = svg.append("g")
                    .selectAll("g")
                    .data(nodeArray)
                    .enter().append("g")
                    .call(d3.drag()
                        .on("start", dragstarted)
                        .on("drag", dragged)
                        .on("end", dragended));
                
                node.append("circle")
                    .attr("r", 10)
                    .attr("fill", "#69b3a2");

                node.append("text")
                    .text(d => d.id)
                    .attr("x", 12)
                    .attr("y", 4)
                    .attr("font-size", 10);

                simulation.on("tick", () => {
                    link.attr("x1", d => d.source.x)
                        .attr("y1", d => d.source.y)
                        .attr("x2", d => d.target.x)
                        .attr("y2", d => d.target.y);

                    node.attr("transform", d => `translate(${d.x},${d.y})`);
                });

                function dragstarted(event, d) {
                    if (!event.active) simulation.alphaTarget(0.3).restart();
                    d.fx = d.x;
                    d.fy = d.y;
                }
                function dragged(event, d) {
                    d.fx = event.x;
                    d.fy = event.y;
                }
                function dragended(event, d) {
                    if (!event.active) simulation.alphaTarget(0);
                    d.fx = null;
                    d.fy = null;
                }
            });
            </script>
            </body></html>
            """;
    }
}
