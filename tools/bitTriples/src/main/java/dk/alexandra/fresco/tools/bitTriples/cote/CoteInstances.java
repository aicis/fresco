package dk.alexandra.fresco.tools.bitTriples.cote;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import java.util.HashMap;
import java.util.Map;

public class CoteInstances {

    private final BitTripleResourcePool resourcePool;
    private final StrictBitVector mac;
    private final Network network;
    private final Map<Integer, Map<Integer, CoteFactory>> coteMap;

    public CoteInstances(BitTripleResourcePool resourcePool, Network network, StrictBitVector mac) {
        this.resourcePool = resourcePool;
        this.network = network;
        this.mac = mac;
        this.coteMap = initializeCote();
    }

    private Map<Integer, Map<Integer, CoteFactory>> initializeCote() {
        Map<Integer, Map<Integer, CoteFactory>> COTeInstances = new HashMap<>();
        for (int receiverId = 1; receiverId <= resourcePool.getNoOfParties(); receiverId++) {
            for (int senderId = 1; senderId <= resourcePool.getNoOfParties(); senderId++) {
                if (receiverId != senderId) {
                    if (resourcePool.getMyId() == senderId) {
                        CoteFactory cote = resourcePool.createCote(receiverId, network, mac);
                        Map<Integer, CoteFactory> map = new HashMap<>();
                        map.put(senderId, cote);
                        COTeInstances.put(receiverId, map);
                    } else if (resourcePool.getMyId() == receiverId) {
                        CoteFactory cote = resourcePool.createCote(senderId, network, mac);

                        Map<Integer, CoteFactory> map = COTeInstances.get(receiverId);
                        if (map == null) {
                            map = new HashMap<>();
                        }
                        map.put(senderId, cote);
                        COTeInstances.put(receiverId, map);
                    }
                }
            }
        }
        return COTeInstances;
    }

    public CoteFactory get(int receiverId, int senderId){
        return coteMap.get(receiverId).get(senderId);
    }
}
