package com.ruipokoim.neonaddon.modules;
import com.ruipokoim.neonaddon.NeonMain;
import meteordevelopment.meteorclient.systems.modules.Module;
public class CrystalOptimizer extends Module{
    public CrystalOptimizer() {
        super(NeonMain.CATEGORY, "crystal-optimizer", "Lets you crystal fast");
    }
    public boolean Get(){
        return isActive();
    }
}