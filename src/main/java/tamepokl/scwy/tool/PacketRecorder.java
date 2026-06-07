package tamepokl.scwy.tool;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import net.minecraft.network.protocol.Packet;
import tamepokl.scwy.Reference;
import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;

public class PacketRecorder extends ExpandableTool {
    public static final PacketRecorder INSTANCE = new PacketRecorder("packetRecorder");
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String AQUA = "\u001B[36m";

    public static final String S2C = GREEN + "S2C" + RESET;
    public static final String C2S = RED + "C2S" + RESET;
    static {
        ToolManager.addTool(INSTANCE);
    }
    public PacketRecorder(String name) {
        super(name);
    }
    public void onClientI(Packet<?> packet){
        printPacket(packet, PacketType.S2C);
    }

    public void onClientO(Packet<?> packet){
        printPacket(packet, PacketType.C2S);
    }

    public void onServerI(Packet<?> packet){

    }
    public void onServerO(Packet<?> packet){

    }
    private String last;
    private void printPacket(Packet<?> packet, PacketType packetType) {
        if(!isEnabled()){
            return;
        }
        String prefix = switch (packetType) {
            case S2C -> S2C;
            case C2S -> C2S;
        };
        String detail = packet.getClass().getSimpleName();
//        ScwyUtils.printMessage(component.append(detail),false);
        if(!detail.equals(last)) {
            Reference.LOGGER.info(prefix + " " + detail);
            last=detail;
        }


    }
    public enum PacketType{
        S2C,
        C2S
    }
}
