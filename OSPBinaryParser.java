package osp;

import crosby.binary.BinaryParser;
import crosby.binary.Osmformat;
import javafx.util.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class OSPBinaryParser extends BinaryParser {

    String key, value;
    boolean nodesParsed;
    HashSet<Long> hsNodes;
    HashMap<Long, Pair<Double, Double>> hmNodes;

    double length;

    public OSPBinaryParser(String key, String value) {
        super();
        this.key = key;
        this.value = value;
        nodesParsed = false;
        length = 0;
        hsNodes = new HashSet<Long>();
        hmNodes = new HashMap<Long, Pair<Double, Double>>();
    }

    @Override
    protected void parseRelations(List<Osmformat.Relation> rels) {
    }

    @Override
    protected void parseDense(Osmformat.DenseNodes nodes) {
        if (!nodesParsed)
            return;
        long lastId=0;
        long lastLat=0;
        long lastLon=0;

        for (int i=0 ; i<nodes.getIdCount() ; i++) {
            lastId += nodes.getId(i);
            lastLat += nodes.getLat(i);
            lastLon += nodes.getLon(i);
            if (hsNodes.contains(lastId)) {
                hmNodes.put(lastId, new Pair(parseLat(lastLat), parseLon(lastLon)));
            }
        }
    }

    @Override
    protected void parseNodes(List<Osmformat.Node> nodes) {
        if (!nodesParsed)
            return;

        for (Osmformat.Node n : nodes) {
            if (hsNodes.contains(n.getId())) {
                hmNodes.put(n.getId(), new Pair(parseLat(n.getLat()), parseLon(n.getLon())));
            }
        }
    }

    @Override
    protected void parseWays(List<Osmformat.Way> ways) {
        for (Osmformat.Way w : ways) {
            for (int i=0 ; i<w.getKeysCount() ; i++) {
                if (getStringById(w.getKeys(i)).trim().toLowerCase().equals(key)
                        && getStringById(w.getVals(i)).trim().toLowerCase().equals(value))
                {
                    if (nodesParsed)
                    {
                        long lastRef = 0;
                        long currRef = 0;
                        for (Long ref : w.getRefsList()) {
                            lastRef = currRef;
                            currRef+= ref;
                            if (lastRef != 0 && currRef != 0) { // both refs already set
                                Pair<Double,Double> first = hmNodes.get(lastRef);
                                Pair<Double,Double> second = hmNodes.get(currRef);
                                length += distance(first.getKey(), first.getValue(), second.getKey(), second.getValue(), "K");
                            }
                        }

                    }
                    else {
                        long lastRef = 0;
                        for (Long ref : w.getRefsList()) {
                            lastRef += ref;
                            hsNodes.add(lastRef);
                        }
                    }

                }
            }
        }
    }

    @Override
    protected void parse(Osmformat.HeaderBlock header) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        if (!nodesParsed) {
            System.out.println("Parsing nodes in the way - start " + df.format(new Date()));
        }
        else
            System.out.println("Counting length - start " + df.format(new Date()));
    }

    public void complete() {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        if (!nodesParsed) {
            System.out.println("Done. " + df.format(new Date()));
            nodesParsed = true;
        }
        else
        {
            System.out.println("Done. " + df.format(new Date()) + "\n");
            System.out.println("Length of way " + key + "/" + value + " is " + String.format("%.3f", length) + " km.");
        }
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(degreesToRadians(lat1)) * Math.sin(degreesToRadians(lat2)) + Math.cos(degreesToRadians(lat1)) * Math.cos(degreesToRadians(lat2)) * Math.cos(degreesToRadians(theta));
        dist = Math.acos(dist);
        dist = radiansToDegrees(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    private static double degreesToRadians(double deg) {
        return (deg * Math.PI / 180.0);
    }


    private static double radiansToDegrees(double rad) {
        return (rad * 180 / Math.PI);
    }

}