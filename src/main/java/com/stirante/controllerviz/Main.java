package com.stirante.controllerviz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stirante.controllerviz.model.AnimationController;
import com.stirante.controllerviz.model.AnimationControllers;
import com.stirante.controllerviz.model.State;
import com.stirante.controllerviz.model.Transition;
import com.stirante.controllerviz.utils.StringUtils;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class Main {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Transition.class, new TransitionDeserializer())
            .create();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            //show usage
        }
        else {
            String s = Pipe.from(new File(args[0])).toString();
            AnimationControllers animationControllers = GSON.fromJson(s, AnimationControllers.class);
            for (String controllerId : animationControllers.animation_controllers.keySet()) {
                AnimationController animationController = animationControllers.animation_controllers.get(controllerId);
                if (animationController.states.size() == 0) {
                    throw new IllegalArgumentException("Animation controller '" + controllerId + "' has no states!");
                }
                String initialState =
                        StringUtils.isNullOrEmpty(animationController.initial_state) ?
                                animationController.states.keySet()
                                        .stream()
                                        .findFirst()
                                        .orElse("") :
                                animationController.initial_state;
                List<Node> nodes = new ArrayList<>();
                Map<String, Node> nodeMap = new HashMap<>();
                for (String stateId : animationController.states.keySet()) {
                    StringBuilder sb = new StringBuilder(stateId);
                    if (animationController.states.get(stateId).on_entry != null) {
                        for (String s1 : animationController.states.get(stateId).on_entry) {
                            sb.append("\n").append(s1);
                        }
                    }
                    nodeMap.put(stateId, node(stateId).with("label", sb.toString()));
                }
                for (String stateId : animationController.states.keySet()) {
                    State state = animationController.states.get(stateId);
                    for (Transition transition : state.transitions) {
                        nodeMap.put(stateId, nodeMap.get(stateId)
                                .link(to(nodeMap.get(transition.targetState)).with("label", transition.condition)));
                    }
                    if (stateId.equals(initialState)) {
                        nodeMap.put(stateId, nodeMap.get(stateId).with(Color.RED));
                        nodes.add(0, nodeMap.get(stateId));
                    }
                    else {
                        nodes.add(nodeMap.get(stateId));
                    }
                }
                Graph g = graph(controllerId).directed()
                        .with(nodes);
                Graphviz.fromGraph(g).render(Format.PNG).toFile(new File(controllerId + ".png"));
            }
        }
    }

}
