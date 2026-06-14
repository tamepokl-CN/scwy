package tamepokl.scwy.tool;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tamepokl.scwy.tool.base.ExpandableTool;
import tamepokl.scwy.tool.base.ToolManager;

public class NoCommandVerify extends ExpandableTool {
    public static final NoCommandVerify INSTANCE = new NoCommandVerify("noCommandVerify",true);
    public NoCommandVerify(String name, boolean defaultValue) {
        super(name, defaultValue);
    }
    public ConfigStringList list = config.ofStringList("list", ImmutableList.of("tpaccept","tpdeny"));
    static {
        ToolManager.addTool(INSTANCE);
    }
    public static void onVerify(String string, CallbackInfoReturnable<ClientPacketListener.CommandCheckResult> cir) {
        boolean checked = false;
        for (String st : INSTANCE.list.getStrings()) {
            if(string.startsWith(st)){
                checked=true;
                break;
            }
        }
        if(checked){
            cir.setReturnValue(ClientPacketListener.CommandCheckResult.NO_ISSUES);
        }
    }
}
